package com.math040.gambling.service;

import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.math040.gambling.GamblingException;
import com.math040.gambling.config.JpaConfig;
import com.math040.gambling.dto.Debt;
import com.math040.gambling.dto.Transaction;
import com.math040.gambling.dto.User;
import com.math040.gambling.repository.TransactionRepository;

import config.TestBasedConfig; 
 

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={TestBasedConfig.class,JpaConfig.class})
@Transactional
@TestExecutionListeners(                
	    { DependencyInjectionTestExecutionListener.class,  
	    	TransactionalTestExecutionListener.class })  
public class TransactionServiceTest {
	@Rule
	public ExpectedException thrown= ExpectedException.none();
	
	@Autowired
	private TransactionService transService;
	
	@Autowired
	private DebtService debtService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TransactionRepository transDao;
	   
	@Test
	@Rollback
	public void testCreateTrans() throws GamblingException { 
		Debt debt = initDebt(); 
		User gambler = initGambler();
		Transaction trans = new Transaction();
		trans.setDebt(debt);
		trans.setGambler(gambler);
		trans.setPredict(Transaction.PREDICT_YES);
		trans.setAmount(29);
		Transaction savedTrans = transService.create(trans);
		Assert.assertNotNull(savedTrans.getId());
		Assert.assertEquals(29, savedTrans.getAmount());
		Assert.assertEquals(trans.getPredict(), savedTrans.getPredict());
		Assert.assertEquals(Transaction.NOT_DEALER, savedTrans.getIsDealer());
	}
	 
	
	@Test
	@Rollback
	public void testCreateTrans_with_2_same_amount_predict_throw_amount_not_correct_exception() throws GamblingException { 
		Debt debt = initDebt(); 
		User gambler = initGambler();
		User gambler2 = initGambler2();
		Transaction trans1 = new Transaction();
		trans1.setDebt(debt);
		trans1.setGambler(gambler);
		trans1.setPredict(Transaction.PREDICT_YES);
		trans1.setAmount(29);
		transService.create(trans1);
		thrown.expect(GamblingException.class);
		thrown.expectMessage(GamblingException.TRANS_AMOUNT_NOT_CORRECT); 
		Transaction trans2 = new Transaction();
		trans2.setDebt(debt);
		trans2.setGambler(gambler2);
		trans2.setPredict(Transaction.PREDICT_YES);
		trans2.setAmount(29);
		transService.create(trans2);
	}
	
	@Rollback
	private Debt initDebt() throws GamblingException {
		Debt debt = new Debt();
		debt.setTitle("first test debt");
		User user = userService.findByUserName("admin");
		debt.setDealer(user); 
		debt.setDeadline(new Date());
		return debtService.create(debt);
	}
	
	@Rollback
	private User initGambler() throws GamblingException {
		User gambler = new User();
		gambler.setUserName("liang");
		gambler.setPassword("123");
		gambler.setRole(User.ROLE_USER); 
		return userService.save(gambler);
	}
	@Rollback
	private User initGambler2() throws GamblingException {
		User gambler = new User();
		gambler.setUserName("liang2");
		gambler.setPassword("123");
		gambler.setRole(User.ROLE_USER); 
		return userService.save(gambler);
	}
	@Test
	@Rollback
	public void testCreateTrans_throw_debt_should_not_gamble_exception() throws GamblingException { 
		Debt debt = initDebt();
		User dealer = userService.findByUserName("admin");
		thrown.expect(GamblingException.class);
		thrown.expectMessage(GamblingException.TRANS_DEBT_SHOULD_NOT_GAMBLE);
		Transaction trans1 = new Transaction();
		trans1.setDebt(debt);
		trans1.setGambler(dealer);
		trans1.setPredict(Transaction.PREDICT_YES);
		trans1.setAmount(29);
		transService.create(trans1); 
	}
	
	
	@Test
	@Rollback
	public void testCreateTrans_throw_GAMBLER_SHOULD_GAMBLE_ONCE_IN_ONE_GAME_exception() throws GamblingException { 
		Debt debt = initDebt(); 
		User gambler = initGambler(); 
		Transaction trans1 = new Transaction();
		trans1.setDebt(debt);
		trans1.setGambler(gambler);
		trans1.setPredict(Transaction.PREDICT_YES);
		trans1.setAmount(29);
		transService.create(trans1);
		thrown.expect(GamblingException.class);
		thrown.expectMessage(GamblingException.TRANS_GAMBLER_SHOULD_GAMBLE_ONCE_IN_ONE_GAME); 
		Transaction trans2 = new Transaction();
		trans2.setDebt(debt);
		trans2.setGambler(gambler);
		trans2.setPredict(Transaction.PREDICT_YES);
		trans2.setAmount(29);
		transService.create(trans2);
	}
	
	@Test
	@Rollback
	public void testEndTransWhenOneWin29AndTheOtherWin23()throws GamblingException{
		Debt debt = initDebt(); 
		User gambler = initGambler(); 
		User gambler2 = initGambler2(); 
		Transaction trans1 = new Transaction();
		trans1.setDebt(debt);
		trans1.setGambler(gambler);
		trans1.setPredict(Transaction.PREDICT_YES);
		trans1.setAmount(29);
		transService.create(trans1);  
		Transaction trans2 = new Transaction();
		trans2.setDebt(debt);
		trans2.setGambler(gambler2);
		trans2.setPredict(Transaction.PREDICT_YES);
		trans2.setAmount(23);
		transService.create(trans2);
		
		debt.setResult(Debt.RESULT_YES);
		transService.end(debt);
		List<Transaction> transList = transDao.findAll(new Sort(Sort.Direction.DESC,"amount"));
		Assert.assertEquals(29, transList.get(0).getWinAmount());
		Assert.assertEquals(23, transList.get(1).getWinAmount());
	}
	
	@Test
	@Rollback
	public void testEndTransWhenOneWin29AndTheOtherLose23()throws GamblingException{
		Debt debt = initDebt(); 
		User gambler = initGambler(); 
		User gambler2 = initGambler2(); 
		Transaction trans1 = new Transaction();
		trans1.setDebt(debt);
		trans1.setGambler(gambler);
		trans1.setPredict(Transaction.PREDICT_YES);
		trans1.setAmount(29);
		transService.create(trans1);  
		Transaction trans2 = new Transaction();
		trans2.setDebt(debt);
		trans2.setGambler(gambler2);
		trans2.setPredict(Transaction.PREDICT_NO);
		trans2.setAmount(23);
		transService.create(trans2);
		
		debt.setResult(Debt.RESULT_YES);
		transService.end(debt);
		List<Transaction> transList = transDao.findAll(new Sort(Sort.Direction.DESC,"amount"));
		Assert.assertEquals(29, transList.get(0).getWinAmount());
		Assert.assertEquals(-23, transList.get(1).getWinAmount());
	}
	
	
	@Test
	@Rollback
	public void testEndTransWhenDealerLoseOneWin29AndTheOtherLose29()throws GamblingException{
		Debt debt = initDebt(); 
		User gambler = initGambler(); 
		User gambler2 = initGambler2(); 
		Transaction trans1 = new Transaction();
		trans1.setDebt(debt);
		trans1.setGambler(gambler);
		trans1.setPredict(Transaction.PREDICT_YES);
		trans1.setAmount(29);
		transService.create(trans1);  
		Transaction trans2 = new Transaction();
		trans2.setDebt(debt);
		trans2.setGambler(gambler2);
		trans2.setPredict(Transaction.PREDICT_NO);
		trans2.setAmount(23);
		transService.create(trans2);
		
		debt.setResult(Debt.RESULT_DEALER_LOSE);
		transService.end(debt);
		List<Transaction> transList = transDao.findAll(new Sort(Sort.Direction.DESC,"amount"));
		Assert.assertEquals(29, transList.get(0).getWinAmount());
		Assert.assertEquals(23, transList.get(1).getWinAmount());
	}
}

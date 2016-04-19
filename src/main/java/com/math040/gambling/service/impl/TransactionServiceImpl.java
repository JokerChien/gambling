package com.math040.gambling.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.math040.gambling.GamblingException;
import com.math040.gambling.dto.Amount;
import com.math040.gambling.dto.Debt;
import com.math040.gambling.dto.Transaction;
import com.math040.gambling.repository.DebtRepository;
import com.math040.gambling.repository.TransactionRepository;
import com.math040.gambling.service.TransactionService; 

@Service
public class TransactionServiceImpl implements TransactionService {
	@Autowired
	TransactionRepository transDao;
	
	@Autowired
	DebtRepository debtDao;
	 
	public Transaction create(Transaction transaction) throws GamblingException{
		Assert.assertNotNull(transaction);
		if(transaction.getGambler()==null || transaction.getGambler().getId()==null){
			throw new GamblingException(GamblingException.TRANS_USER_ID_SHOULD_NOT_NULL);
		}
		if(transaction.getDebt()==null || transaction.getDebt().getId()==null){
			throw new GamblingException(GamblingException.TRANS_DEBT_ID_SHOULD_NOT_NULL);
		} 
		Debt debt = debtDao.findOne(transaction.getDebt().getId());
		if(debt==null){
			throw new GamblingException(GamblingException.TRANS_DEBT_ID_SHOULD_NOT_NULL);
		}
		if(debt.getDealer().equals(transaction.getGambler())){
			throw new GamblingException(GamblingException.TRANS_DEBT_SHOULD_NOT_GAMBLE);
		}
		transaction.setDebt(debt);
		transaction.setIsDealer(Transaction.NOT_DEALER);
		if(!validateOneShouldDebtOnceInOneDebt(transaction)){
			throw new GamblingException(GamblingException.TRANS_GAMBLER_SHOULD_GAMBLE_ONCE_IN_ONE_GAME);
		}
		if(!transaction.validatePredict()){
			throw new GamblingException(GamblingException.TRANS_PREDICT_NOT_CORRECT);
		}
		if(!checkAmountAvailable(transaction)){
			throw new GamblingException(GamblingException.TRANS_AMOUNT_NOT_CORRECT);
		}
		return transDao.save(transaction);
		
	}
	
	private boolean checkAmountAvailable(Transaction transaction) throws GamblingException {
		Assert.assertNotNull(transaction);
		Assert.assertNotNull(transaction.getDebt());
		Assert.assertNotNull(transaction.getDebt().getId()); 
		if(!transaction.validatePredict()){
			throw new GamblingException(GamblingException.TRANS_PREDICT_NOT_CORRECT);
		}
		List<Transaction> predictedTrans = transDao.findByDebt_IdAndPredict(transaction.getDebt().getId(), transaction.getPredict());
		List<Integer> predictedAmounts = new ArrayList<>();
		if(!CollectionUtils.isEmpty(predictedTrans)){
			for(Transaction trans:predictedTrans){
				predictedAmounts.add(trans.getAmount());
			}
		} 
		return Amount.validate(predictedAmounts, transaction.getAmount());
	}
	 
	private boolean validateOneShouldDebtOnceInOneDebt(Transaction transaction){
		List<Transaction> trans = transDao.findByDebt_idAndGambler_id(transaction.getDebt().getId(), transaction.getGambler().getId());
		if(CollectionUtils.isEmpty(trans)){
			return true;
		}
		return false;
	}

	@Override
	public void end(Debt debt) throws GamblingException {
		 Assert.assertNotNull(debt);
		 Assert.assertNotNull(debt.getId()); 
		 if(Debt.RESULT_YES.equals(debt.getResult())){
			 transDao.setWinAmountWhenWin(debt, Debt.RESULT_YES);
			 transDao.setWinAmountWhenLose(debt, Debt.RESULT_NO);
			 return;
		 } 
		 if(Debt.RESULT_NO.equals(debt.getResult())){
			 transDao.setWinAmountWhenWin(debt, Debt.RESULT_NO);
			 transDao.setWinAmountWhenLose(debt, Debt.RESULT_YES);
			 return;
		 } 
		 if(Debt.RESULT_DEALER_LOSE.equals(debt.getResult())){
			 transDao.setWinAmountWhenWin(debt, Debt.RESULT_NO);
			 transDao.setWinAmountWhenWin(debt, Debt.RESULT_YES);
			 return;
		 } 
	}
}

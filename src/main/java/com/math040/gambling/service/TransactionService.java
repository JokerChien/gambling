package com.math040.gambling.service;

import com.math040.gambling.GamblingException;
import com.math040.gambling.dto.Debt;
import com.math040.gambling.dto.Transaction;

public interface TransactionService {  
 Transaction create(Transaction transaction) throws GamblingException; 
  void end(Debt debt)throws GamblingException; 
}

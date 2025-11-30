package com.banking.Banking.Service;

import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository repository;

    public Transaction createTransaction(Transaction transaction){
        if (transaction.getSenderCard() == null ||
            transaction.getReceiverCard() == null ||
            transaction.getReceiverCard().equals(transaction.getSenderCard()) ||
            transaction.getAmount().compareTo(new BigDecimal("0")) <= 0){
            return null;
        }
        transaction.setTimestamp(LocalDateTime.now());
        repository.save(transaction);
        return transaction;
    }

    public List<Transaction> findByCardId(Long cardId){
        return repository.findByCardId(cardId);
    }
}

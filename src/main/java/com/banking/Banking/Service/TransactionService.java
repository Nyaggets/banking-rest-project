package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private CardService cardService;

    public Transaction createTransaction(Transaction transaction, Long senderCardId){
        Card senderCard = cardService.findById(senderCardId);
        if (senderCard == null || transaction.getReceiverCard() == null){
            return null;
        }
        transaction.setSenderCard(senderCard);
        if (transaction.getReceiverCard() == null ||
            transaction.getReceiverCard().equals(transaction.getSenderCard()) ||
            transaction.getAmount().compareTo(new BigDecimal("0")) <= 0){
            return null;
        }
        transaction.setTimestamp(LocalDateTime.now());
        repository.save(transaction);
        return transaction;
    }

    public Page<Transaction> findByCardId(Long cardId, Pageable pageable){
        return repository.findByCardId(cardId, pageable);
    }
}

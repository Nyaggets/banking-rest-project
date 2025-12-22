package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN t.senderCard s LEFT JOIN t.receiverCard r " +
            "WHERE s.id = :cardId OR r.id = :cardId")
    List<Transaction> findByCardId(Long cardId);
}

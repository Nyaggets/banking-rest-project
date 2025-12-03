package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("SELECT t FROM Transaction t " +
            "JOIN t.senderCard s JOIN t.receiverCard r " +
            "WHERE s.id = :cardId OR r.id = :cardId")
    Page<Transaction> findByCardId(Long cardId, Pageable pageable);
}

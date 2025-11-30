package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByCardId(Long cardId);
}

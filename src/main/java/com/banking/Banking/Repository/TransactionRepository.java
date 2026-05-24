package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    @Query("""
        SELECT DISTINCT t FROM Transaction t
        LEFT JOIN t.clientCard c
        WHERE c.id = :cardId
    """)
    List<Transaction> findByCardId(Long cardId);
    Page<Transaction> findAll(Specification<Transaction> spec, Pageable pageable);
}

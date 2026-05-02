package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByCardNumber(String cardNumber);
    @Query("SELECT ca FROM Card ca " +
            "JOIN ca.client cl " +
            "WHERE cl.id = :id")
    List<Card> findAllByClientId(Long id);
    @Query("SELECT ca FROM Card ca WHERE ca.cardNumberHash LIKE :cardNumber")
    Optional<Card> findByCardNumberHash(String cardNumber);
    @Query("SELECT ca FROM Card ca WHERE ca.cardNumberHash LIKE CONCAT('%', :last4)")
    Optional<Card> findHiddenNumber(String last4);
    @Query("SELECT ca FROM Card ca WHERE ca.cvvHash = :cvv")
    Optional<Card> findByCVV(String cvv);
}

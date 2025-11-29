package com.banking.Banking.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    Long id;
    @ManyToOne
    @JoinColumn(name = "sender_card_id")
    Card senderCard;
    @ManyToOne
    @JoinColumn(name = "receiver_card_id")
    Card receiverCard;
    BigDecimal amount;
    LocalDateTime timestamp;
    String description;
}

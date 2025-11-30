package com.banking.Banking.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
@Getter
@Setter
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

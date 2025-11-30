package com.banking.Banking.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PACKAGE)
@Getter
@Setter
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    Long id;
    String cardNumber;
    BigDecimal balance;
    @ManyToOne
    @JoinColumn(name = "client_id")
    Client client;
    LocalDate createdDate;
}

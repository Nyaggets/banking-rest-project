package com.banking.Banking.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PACKAGE)
@Getter
@Setter
@Table(name = "card")
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    Long id;
    @Column(name = "card_number", columnDefinition = "TEXT")
    @NotBlank
    String cardNumber;
    @NotNull
    BigDecimal balance;
    @NotNull
    @ManyToOne
    @JoinColumn(name = "client_id")
    Client client;
    @Column(name = "client_name")
    @NotBlank
    String clientName;
    @Column(name = "created_date")
    @NotNull
    LocalDate createdDate;
    @Column(name = "expired_date")
    @NotNull
    LocalDate expiredDate;
    @NotNull
    String cvv;
    @NotNull
    String last4;
    @NotNull
    @Column(name = "card_number_hash")
    String cardNumberHash;
    @NotNull
    @Column(name = "cvv_hash")
    String cvvHash;
    @NotNull
    @Column(name = "pin_code")
    String pinCode;
    @NotNull
    @Column(name = "pin_code_hash")
    String pinCodeHash;
    @NotNull
    @Column(name = "account_number", unique = true)
    @Size(min = 20, max = 20)
    String accountNumber;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return Objects.equals(balance, card.balance);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(balance);
    }
}

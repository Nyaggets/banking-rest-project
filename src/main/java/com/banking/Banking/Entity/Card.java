package com.banking.Banking.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
    @NotBlank
    @Pattern(regexp = "^\\d{20}$", message = "Номер карты должен содержать 20 символов")
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
    @NotNull
    @PastOrPresent(message = "Дата создания карты должна быть не позже текущей даты")
    LocalDate createdDate;

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

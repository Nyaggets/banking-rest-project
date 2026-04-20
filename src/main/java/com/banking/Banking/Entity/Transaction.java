package com.banking.Banking.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PACKAGE)
@Getter
@Setter
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    Long id;
    @Column(name = "operation_type")
    @Enumerated(EnumType.STRING)
    OperationTypes type;
    @ColumnDefault("null")
    String merchant;
    @ColumnDefault("null")
    String source;
    @ManyToOne
    @JoinColumn(name = "sender_card_id")
    @ColumnDefault("null")
    Card senderCard;
    @ManyToOne
    @JoinColumn(name = "receiver_card_id")
    @ColumnDefault("null")
    Card receiverCard;
    @Positive(message = "Сумма перевода должна быть больше 0")
    BigDecimal amount;
    @PositiveOrZero(message = "Сумма комиссии должна быть больше или равна 0")
    @ColumnDefault("0")
    BigDecimal commission;
    @Column(name = "total_amount")
    @Positive(message = "Сумма перевода должна быть больше 0")
    @ColumnDefault("0")
    BigDecimal totalAmount;
    @PastOrPresent(message = "Дата перевода должна быть не позже текущей")
    LocalDateTime timestamp;
    @ColumnDefault("null")
    String description;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

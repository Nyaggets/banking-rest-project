package com.banking.Banking.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

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
    @Column(name = "transfer_id")
    UUID transferId;
    @Column(name = "is_internal")
    Boolean isInternal;
    @ManyToOne
    @JoinColumn(name = "client_card_id")
    @NotNull
    Card clientCard;
    @NotNull
    String counterPartyName;
    String counterPartyHiddenNumber;
    @Positive(message = "Сумма перевода должна быть больше 0")
    BigDecimal amount;
    @PositiveOrZero(message = "Сумма комиссии должна быть больше или равна 0")
    BigDecimal commission = BigDecimal.ZERO;
    @Column(name = "total_amount")
    @Positive(message = "Сумма перевода должна быть больше 0")
    BigDecimal totalAmount;
    @PastOrPresent(message = "Дата перевода должна быть не позже текущей")
    LocalDateTime timestamp;
    String description;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        if (((Transaction) o).transferId != null)
            return Objects.equals(transferId, that.transferId);
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public String getHiddenCard() {
        return "****" + this.getClientCard().getLast4();
    }

    public String getDirection() {
        if (this.isInternal)
            return "between";
        return switch (this.type) {
            case DEPOSIT, TRANSFER_IN -> "in";
            case WITHDRAWAL, TRANSFER_OUT -> "out";
        };
    }
}

package com.banking.Banking.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

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
    @Column(name = "operation_type")
    String type;
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
    BigDecimal amount;
    LocalDateTime timestamp;
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

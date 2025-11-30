package com.banking.Banking.Dto;

import com.banking.Banking.Entity.Card;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDtoResponse {
    Long senderCardId;
    Long receiverCardId;
    BigDecimal amount;
    LocalDateTime timestamp;
    String description;
}

package com.banking.Banking.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDtoResponse {
    String type;
    String merchant;
    String source;
    Long senderCardId;
    Long receiverCardId;
    BigDecimal amount;
    LocalDateTime timestamp;
    String description;
}

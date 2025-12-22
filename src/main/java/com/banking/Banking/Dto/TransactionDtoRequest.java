package com.banking.Banking.Dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionDtoRequest {
    String type;
    String merchant;
    String source;
    Long senderCardId;
    Long receiverCardId;
    BigDecimal amount;
    String description;
}

package com.banking.Banking.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDtoResponse {
    Long id;
    String type;
    String merchant;
    String source;
    BigDecimal amount;
    LocalDateTime timestamp;
    String description;

    Long senderCardId;
    String senderCardNumber;
    ClientDtoResponse senderDetails;

    Long receiverCardId;
    String receiverCardNumber;
    ClientDtoResponse receiverDetails;
}

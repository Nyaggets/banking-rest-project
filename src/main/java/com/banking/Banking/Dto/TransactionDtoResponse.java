package com.banking.Banking.Dto;

import com.banking.Banking.Entity.OperationTypes;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDtoResponse {
    Long id;
    OperationTypes type;
    String merchant;
    String source;
    BigDecimal amount;
    BigDecimal commission;
    BigDecimal totalAmount;
    LocalDateTime timestamp;
    String description;

    Long senderCardId;
    String senderCardNumber;
    ClientDtoResponse senderDetails;

    Long receiverCardId;
    String receiverIdentifier;
    ClientDtoResponse receiverDetails;
}

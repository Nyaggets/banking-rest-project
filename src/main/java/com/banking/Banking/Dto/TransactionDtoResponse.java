package com.banking.Banking.Dto;

import com.banking.Banking.Entity.OperationTypes;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDtoResponse {
    Long id;
    String direction;
    OperationTypes type;
    BigDecimal amount;
    BigDecimal commission;
    BigDecimal totalAmount;
    LocalDateTime timestamp;
    String description;
    String isInternal;

    Long clientCardId;
    String clientHiddenNumber;

    String counterPartyName;
    String counterPartyHiddenNumber;
}

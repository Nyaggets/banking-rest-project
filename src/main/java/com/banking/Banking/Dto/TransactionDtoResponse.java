package com.banking.Banking.Dto;

import com.banking.Banking.Entity.CounterpartyTypeEnum;
import com.banking.Banking.Entity.OperationTypeEnum;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionDtoResponse {
    Long id;
    String direction;
    OperationTypeEnum operationType;
    String isInternal;
    BigDecimal totalAmount;
    BigDecimal commission;
    LocalDateTime timestamp;
    String description;

    Long clientCardId;
    String clientHiddenNumber;

    String counterpartyName;
    String counterpartyIdentifier;
    CounterpartyTypeEnum counterpartyType;
}
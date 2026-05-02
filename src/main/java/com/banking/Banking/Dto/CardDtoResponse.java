package com.banking.Banking.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDtoResponse {
    Long id;
    String hiddenNumber;
    BigDecimal balance;
    Long clientId;
    LocalDate expiredDate;
}


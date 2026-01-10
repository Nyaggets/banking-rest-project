package com.banking.Banking.Dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDtoRequest {
    Long id;
    String cardNumber;
    BigDecimal balance;
    Long clientId;
    LocalDate createdDate;
}


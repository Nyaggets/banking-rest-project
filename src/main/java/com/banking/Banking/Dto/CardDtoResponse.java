package com.banking.Banking.Dto;

import com.banking.Banking.Entity.Client;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardDtoResponse {
    String cardNumber;
    BigDecimal balance;
    Long clientId;
    LocalDate createdDate;
}

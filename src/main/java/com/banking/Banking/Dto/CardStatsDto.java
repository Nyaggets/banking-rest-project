package com.banking.Banking.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data @AllArgsConstructor
public class CardStatsDto {
    BigDecimal income;
    BigDecimal outcome;
}

package com.banking.Banking.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WithdrawalDtoRequest {
    @NotNull(message = "{field.required}")
    Long clientCardId;

    @NotNull(message = "{field.required}")
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ\\d\\s\\p{Punct}]{5,50}$", message = "{cardOrPhone.invalidPattern}")
    String counterpartyIdentifier;

    @Size(min = 3, max = 200)
    String counterpartyName;

    @NotNull(message = "{field.required}")
    @Positive(message = "{amount.invalid}")
    BigDecimal amount;
}

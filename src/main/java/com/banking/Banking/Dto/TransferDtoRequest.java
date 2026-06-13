package com.banking.Banking.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransferDtoRequest {
    @NotNull(message = "{field.required}")
    Long clientCardId;

    @NotNull(message = "{field.required}")
    @Pattern(regexp = "^(\\d{4}|(\\+7|8)\\d{10}|\\d*)$", message = "{cardOrPhone.invalidPattern}") //не понятно как указать ошибку айдишника
    String counterpartyCardIdentifier;

    @NotNull(message = "{field.required}")
    @Positive(message = "{amount.invalid}")
    BigDecimal amount;

    @Nullable
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ0-9\\s\\p{Punct}]{0,50}$", message = "{description.invalid}")
    String description;
}

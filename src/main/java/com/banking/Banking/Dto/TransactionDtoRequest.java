package com.banking.Banking.Dto;

import com.banking.Banking.validation.TransferGroup;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDtoRequest {
    @NotNull(message = "{field.required}")
    Long clientCardId;

    @NotNull(message = "{field.required}")
    @Pattern(regexp = "^(\\d{4}|(\\+7|8)\\d{10}|\\d*)$", message = "{cardOrPhone.invalidPattern}")
    String counterPartyCardId;

    @NotNull(message = "{field.required}")
    @Pattern(regexp = "^(\\d{4}|(\\+7|8)\\d{10}|\\d*)$", message = "{cardOrPhone.invalidPattern}")
    String counterPartyIdentifier;

    @NotNull(message = "{field.required}")
    @Positive(message = "{amount.invalid}")
    BigDecimal amount;

    @Nullable
    @Pattern(regexp = "^[a-zA-Zа-яёА-ЯЁ0-9\\s\\p{Punct}]{0,50}$", groups = {TransferGroup.class}, message = "{description.invalid}")
    String description;
}

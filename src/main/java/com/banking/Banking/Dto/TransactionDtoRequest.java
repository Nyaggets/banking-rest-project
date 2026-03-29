package com.banking.Banking.Dto;

import com.banking.Banking.validation.DepositGroup;
import com.banking.Banking.validation.TransferGroup;
import com.banking.Banking.validation.WithdrawalGroup;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
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
public class TransactionDtoRequest {
    @Null(groups = {TransferGroup.class, DepositGroup.class}, message = "{field.invalidOperation}")
    @NotNull(groups = {WithdrawalGroup.class}, message = "{field.required}")
    String merchant;

    @Null(groups = {TransferGroup.class, WithdrawalGroup.class}, message = "{field.invalidOperation}")
    @NotNull(groups = {DepositGroup.class}, message = "{field.required}")
    String source;

    @Null(groups = {DepositGroup.class}, message = "{field.invalidOperation}")
    @NotNull(groups = {TransferGroup.class, WithdrawalGroup.class}, message = "{field.required}")
    Long senderCardId;

    @Null(groups = {WithdrawalGroup.class}, message = "{field.invalidOperation}")
    @NotNull(groups = {TransferGroup.class, DepositGroup.class}, message = "{field.required}")
    @Pattern(regexp = "^\\d{20}$", message = "{card.invalidPattern}", groups = {TransferGroup.class})
    String receiverCardNumber;

    @NotNull(message = "{field.required}")
    @Positive(message = "{amount.invalid}")
    BigDecimal amount;

    @Nullable
    String description;
}

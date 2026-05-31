package com.banking.Banking.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSafeDataDto {
    @Nullable
    @Pattern(regexp = "^(8|\\+7)[\\s\\-]?\\d{3}[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$", message = "{phone.invalidPattern}")
    String phone;
    @Nullable
    @Size(min = 2, max = 40, message = "{login.invalid}")
    String login;
}

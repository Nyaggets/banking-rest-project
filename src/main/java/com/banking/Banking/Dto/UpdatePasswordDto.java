package com.banking.Banking.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDto {
    @NotNull
    @Size(min = 5, max = 30, message = "{password.invalid}")
    String oldPassword;
    @NotNull
    @Size(min = 5, max = 30, message = "{password.invalid}")
    String newPassword;
    @NotNull
    @Size(min = 5, max = 30, message = "{password.invalid}")
    String passwordConf;
}

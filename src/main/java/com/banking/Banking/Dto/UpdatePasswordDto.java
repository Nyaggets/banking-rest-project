package com.banking.Banking.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordDto {
    @NotNull
    @Pattern(regexp = "^(?=(?:.*[A-Z])+)(?=.*[0-9])(?=.*[-_])[A-Za-z0-9_-]{5,30}$", message = "{password.invalidPattern}")
    String oldPassword;
    @NotNull
    @Pattern(regexp = "^(?=(?:.*[A-Z])+)(?=.*[0-9])(?=.*[-_])[A-Za-z0-9_-]{5,30}$", message = "{password.invalidPattern}")
    String newPassword;
    @NotNull
    @Pattern(regexp = "^(?=(?:.*[A-Z])+)(?=.*[0-9])(?=.*[-_])[A-Za-z0-9_-]{5,30}$", message = "{password.invalidPattern}")
    String passwordConf;
}

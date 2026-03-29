package com.banking.Banking.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@NotBlank
public class ClientDtoRequest {
    @Pattern(regexp = "^?=(8|\\+7)?[0-9]{3} ?[0-9]{3}?=( |-)?[0-9]{2}?=( |-)?[0-9]{2}$", message = "{phone.invalidPattern}")
    String phone;
    @Size(min = 6, max = 30, message = "{password.invalid}")
    String password;
    @Size(min = 2, max = 40, message = "{login.invalid}")
    String username;
    @Size(min = 2, max = 20, message = "{name.invalid}")
    String name;
}

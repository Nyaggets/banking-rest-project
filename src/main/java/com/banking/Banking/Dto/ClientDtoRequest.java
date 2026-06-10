package com.banking.Banking.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
@NotBlank
public class ClientDtoRequest {
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "^(8|\\+7)[\\s\\-]?\\d{3}[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$", message = "{phone.invalidPattern}")
    String phone;
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "^(?=(?:.*[A-Z])+)(?=.*[0-9])(?=.*[-_])[A-Za-z0-9_-]{5,30}$", message = "{password.invalidPattern}")
    String password;
    @NotNull(message = "{field.required}")
    @Size(min = 4, max = 20, message = "{login.invalid}")
    String login;
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "а-яёА-ЯЁ{2,20}", message = "{name.invalid}")
    String name;
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "а-яёА-ЯЁ{5,20}(-а-яёА-ЯЁ{2,20})?", message = "{surname.invalid}")
    String surname;
    @Nullable
    @Pattern(regexp = "а-яёА-ЯЁ{7,30}", message = "{patronymic.invalid}")
    String patronymic;
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "\\d{4}", message = "{passportSeries.invalid}")
    String passportSeries;
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "\\d{6}", message = "{passportNumber.invalid}")
    String passportNumber;
    @NotNull(message = "{field.required}")
    LocalDate passportIssueDate;
    @NotNull(message = "{field.required}")
    @FutureOrPresent(message = "{passportIssuedBy.invalid}")
    String passportIssuedBy;
    @NotNull(message = "{field.required}")
    @Pattern(regexp = "\\d{3}-\\d{3}", message = "{passportDepartmentCode.invalid}")
    String passportDepartmentCode;
}

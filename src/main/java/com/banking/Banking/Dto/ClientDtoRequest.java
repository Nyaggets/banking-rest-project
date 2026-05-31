package com.banking.Banking.Dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@NotBlank
public class ClientDtoRequest {
    @Pattern(regexp = "^(8|\\+7)[\\s\\-]?\\d{3}[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}$", message = "{phone.invalidPattern}")
    String phone;
    @Size(min = 6, max = 30, message = "{password.invalid}")
    String password;
    @Size(min = 4, max = 20, message = "{login.invalid}")
    String login;
    @Pattern(regexp = "а-яёА-ЯЁ{2,20}", message = "{name.invalid}")
    String name;
    @Pattern(regexp = "а-яёА-ЯЁ{5,20}(-а-яёА-ЯЁ{2,20})?", message = "{surname.invalid}")
    String surname;
    @Pattern(regexp = "а-яёА-ЯЁ{7,30}", message = "{patronymic.invalid}")
    String patronymic;
    @Pattern(regexp = "\\d{4}", message = "{passportSeries.invalid}")
    String passportSeries;
    @Pattern(regexp = "\\d{6}", message = "{passportNumber.invalid}")
    String passportNumber;
    LocalDate passportIssueDate;
    @FutureOrPresent(message = "{passportIssuedBy.invalid}")
    String passportIssuedBy;
    @Pattern(regexp = "\\d{3}-\\d{3}", message = "{passportDepartmentCode.invalid}")
    String passportDepartmentCode;
}

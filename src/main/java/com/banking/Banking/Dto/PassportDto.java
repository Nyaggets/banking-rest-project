package com.banking.Banking.Dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PassportDto {
    String fullName;
    String  series;
    String number;
    LocalDate issueDate;
    String issuedBy;
    String departmentCode;
}

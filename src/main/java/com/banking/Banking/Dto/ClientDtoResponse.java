package com.banking.Banking.Dto;

import lombok.Data;

@Data
public class ClientDtoResponse {
    Long id;
    String login;
    String phone;
    String name;
    String surname;
    String patronymic;
}

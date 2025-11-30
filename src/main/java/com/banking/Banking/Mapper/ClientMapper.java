package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ClientMapper {
    Client fromDtoRequest(ClientDtoRequest clientDtoRequest);
    ClientDtoResponse toDtoResponse(Client client);
}

package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Entity.Client;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    Client fromDtoRequest(ClientDtoRequest clientDtoRequest);
    ClientDtoResponse toDtoResponse(Client client);
    List<ClientDtoResponse> toListDtoResponse(List<Client> clients);
}

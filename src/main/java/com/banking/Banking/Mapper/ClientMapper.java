package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Entity.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    @Mapping(target = "name", source = "name")
    Client fromDtoRequest(ClientDtoRequest clientDtoRequest);
    @Mapping(target = "name", source = "name")
    ClientDtoResponse toDtoResponse(Client client);
    @Mapping(target = "name", source = "name")
    List<ClientDtoResponse> toListDtoResponse(List<Client> clients);
}

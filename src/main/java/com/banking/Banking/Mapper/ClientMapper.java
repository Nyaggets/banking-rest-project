package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClientMapper {
    ClientDtoResponse toDtoResponse(Client client);
}

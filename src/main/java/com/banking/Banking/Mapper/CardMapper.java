package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.CardDtoRequest;
import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target="client.id", source = "clientId")
    Card fromDtoRequest(CardDtoRequest cardDtoRequest);
    @Mapping(target="clientId", source = "client.id")
    CardDtoResponse toDtoResponse(Card card);
}

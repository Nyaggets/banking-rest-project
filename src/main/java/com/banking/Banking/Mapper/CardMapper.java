package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.CardDtoRequest;
import com.banking.Banking.Entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target="client.id", source = "clientId")
    Card fromDto(CardDtoRequest cardDtoRequest);
    @Mapping(target="clientId", source = "client.id")
    CardDtoRequest toDto(Card card);
    @Mapping(target="clientId", source = "client.id")
    List<CardDtoRequest> toListDto(List<Card> cards);
}

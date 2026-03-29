package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target="client.id", source = "clientId")
    Card fromDto(CardDtoResponse cardDtoResponse);
    @Mapping(target="clientId", source = "client.id")
    CardDtoResponse toDto(Card card);
    @Mapping(target="clientId", source = "client.id")
    List<CardDtoResponse> toListDto(List<Card> cards);
}

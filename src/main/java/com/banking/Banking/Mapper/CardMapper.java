package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.validation.CardNotFoundException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CardMapper {
    @Mapping(target="client.id", source = "clientId")
    Card fromDto(CardDtoResponse cardDtoResponse);
    @Mapping(target="clientId", source = "client.id")
    @Mapping(target="hiddenNumber", expression = "java(\"****\" + card.getLast4())")
    CardDtoResponse toDto(Card card);
    @Mapping(target="clientId", source = "client.id")
    @Mapping(target="hiddenNumber", expression = "java(\"****\" + card.getLast4())")
    List<CardDtoResponse> toListDto(List<Card> cards);
}

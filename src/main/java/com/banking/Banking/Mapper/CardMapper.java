package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardMapper {
    @Mapping(target="clientId", source = "client.id")
    @Mapping(target="hiddenNumber", expression = "java(\"****\" + card.getLast4())")
    CardDtoResponse toDto(Card card);
    @Mapping(target="clientId", source = "client.id")
    @Mapping(target="hiddenNumber", expression = "java(\"****\" + card.getLast4())")
    List<CardDtoResponse> toListDto(List<Card> cards);
}

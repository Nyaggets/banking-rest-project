package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    @Mapping(target = "clientCardId", source = "clientCard.id")
    @Mapping(target="clientHiddenNumber", expression = "java(transaction.getHiddenCard())")
    @Mapping(target="direction", expression = "java(transaction.getDirection())")
    @Mapping(target="counterPartyHiddenNumber", expression = "java(\"****\" + transaction.getCounterPartyHiddenNumber())")
    TransactionDtoResponse toDto(Transaction transaction);
    @Mapping(target = "clientCardId", source = "clientCard.id")
    @Mapping(target="clientHiddenNumber", expression = "java(transaction.getHiddenCard())")
    @Mapping(target="direction", expression = "java(transaction.getDirection())")
    @Mapping(target="counterPartyHiddenNumber", expression = "java(\"****\" + transaction.getCounterPartyHiddenNumber())")
    List<TransactionDtoResponse> toDtoList(List<Transaction> transactionList);
}
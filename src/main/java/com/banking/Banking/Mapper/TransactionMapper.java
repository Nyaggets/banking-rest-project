package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Service.CardService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    @Mapping(target = "senderCardId", source = "senderCard.id")
    @Mapping(target = "receiverCardNumber", source = "receiverCard.cardNumber")
    @Mapping(target = "senderDetails", source = "senderCard.client")
    @Mapping(target = "receiverDetails", source = "receiverCard.client")
    TransactionDtoResponse toDto(Transaction transaction);
    @Mapping(target = "senderCardId", source = "senderCard.id")
    @Mapping(target = "receiverCardNumber", source = "receiverCard.cardNumber")
    @Mapping(target = "senderDetails", source = "senderCard.client")
    @Mapping(target = "receiverDetails", source = "receiverCard.client")
    List<TransactionDtoResponse> toDtoList(List<Transaction> transactionList);
}
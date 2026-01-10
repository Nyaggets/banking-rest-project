package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    @Mapping(target = "senderCard.id", source = "senderCardId")
    @Mapping(target = "receiverCard.id", source = "receiverCardId")
    Transaction fromDto(TransactionDtoRequest transactionDtoRequest);
    @Mapping(target = "senderCardId", source = "senderCard.id")
    @Mapping(target = "receiverCardId", source = "receiverCard.id")
    @Mapping(target = "senderCardNumber", source = "senderCard.cardNumber")
    @Mapping(target = "receiverCardNumber", source = "receiverCard.cardNumber")
    @Mapping(target = "senderDetails", source = "senderCard.client")
    @Mapping(target = "receiverDetails", source = "receiverCard.client")
    TransactionDtoResponse toDto(Transaction transaction);
    @Mapping(target = "senderCardId", source = "senderCard.id")
    @Mapping(target = "receiverCardId", source = "receiverCard.id")
    @Mapping(target = "senderCardNumber", source = "senderCard.cardNumber")
    @Mapping(target = "receiverCardNumber", source = "receiverCard.cardNumber")
    @Mapping(target = "senderDetails", source = "senderCard.client")
    @Mapping(target = "receiverDetails", source = "receiverCard.client")
    List<TransactionDtoResponse> toDtoList(List<Transaction> transactionList);
}

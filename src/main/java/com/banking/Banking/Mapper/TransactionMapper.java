package com.banking.Banking.Mapper;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(target = "receiverCard.id", source = "receiverCardId")
    Transaction fromDtoRequest(TransactionDtoRequest transactionDtoRequest);
    @Mapping(target = "senderCardId", source = "senderCard.id")
    @Mapping(target = "receiverCardId", source = "receiverCard.id")
    TransactionDtoResponse toDtoResponse(Transaction transaction);
}

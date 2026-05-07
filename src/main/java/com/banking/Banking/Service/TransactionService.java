package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TransactionService {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private CardService cardService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private TransactionValidationService validationService;

    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount.compareTo(new BigDecimal("100000")) < 0
                ? BigDecimal.ZERO
                : amount.multiply(new BigDecimal("0.05"));
    }

    public Transaction createTransfer(TransactionDtoRequest transactionDto) {
        validationService.validateOperation(OperationTypes.TRANSFER, transactionDto);

        Card senderCard = cardService.findById(transactionDto.getSenderCardId());
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        BigDecimal commission = calculateCommission(transactionDto.getAmount());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.TRANSFER)
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .amount(transactionDto.getAmount())
                .commission(commission)
                .totalAmount(transactionDto.getAmount().add(commission))
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = transaction.getSenderCard().getBalance();
        transaction.getSenderCard().setBalance(senderBalance.subtract(transaction.getTotalAmount()));
        receiverCard.setBalance(receiverCard.getBalance().add(transaction.getAmount()));
        return repository.save(transaction);
    }

    public Transaction createWithdrawal(TransactionDtoRequest transactionDto) {
        validationService.validateOperation(OperationTypes.WITHDRAWAL, transactionDto);

        Card senderCard = cardService.findById(transactionDto.getSenderCardId());
        BigDecimal commission = calculateCommission(transactionDto.getAmount());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.WITHDRAWAL)
                .senderCard(senderCard)
                .merchant(transactionDto.getMerchant())
                .amount(transactionDto.getAmount())
                .commission(commission)
                .totalAmount(transactionDto.getAmount().add(commission))
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal cardBalance = senderCard.getBalance();
        senderCard.setBalance(cardBalance.subtract(transaction.getTotalAmount()));
        return repository.save(transaction);
    }

    public Transaction createDeposit(TransactionDtoRequest transactionDto) {
        validationService.validateOperation(OperationTypes.DEPOSIT, transactionDto);

        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.DEPOSIT)
                .source(transactionDto.getSource())
                .receiverCard(receiverCard)
                .amount(transactionDto.getAmount())
                .totalAmount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = receiverCard.getBalance();
        receiverCard.setBalance(senderBalance.add(transaction.getTotalAmount()));
        return repository.save(transaction);
    }

    public List<Transaction> findByCardId(Long cardId) {
        return repository.findByCardId(cardId);
    }

    public List<Transaction> findByClientId(Long clientId) {
        List<Card> cards = cardService.findByClientId(clientId);
        return cards.stream()
                    .flatMap(card -> repository.findByCardId(card.getId()).stream())
                    .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                    .distinct()
                    .toList();
    }
}

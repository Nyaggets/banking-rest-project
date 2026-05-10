package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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


    public List<Transaction> findTransactions(Long clientId) {
        clientService.findByIdOrThrow(clientId);
        List<Card> cards = cardService.findByClientId(clientId);
        return cards.stream()
                .flatMap(card -> repository.findByCardId(card.getId()).stream())
                .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                .distinct()
                .toList();
    }

    public List<Transaction> findTransactions(Long clientId, @Nullable Long cardId, @Nullable OperationTypes type,
                                              @Nullable String start, @Nullable String end) throws AccessDeniedException {
        clientService.findByIdOrThrow(clientId);
        if (cardId != null && !cardService.belongsToClient(clientId, cardId))
            throw new AccessDeniedException("Доступ к карте запрещен");

        List<Card> cards = cardService.findByClientId(clientId);
        var transactions = cards.stream()
                    .flatMap(card -> repository.findByCardId(card.getId()).stream())
                    .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                    .distinct();
        if (cardId != null)
            transactions = transactions.filter(transaction -> {
                if (transaction.getSenderCard() != null && transaction.getReceiverCard() != null)
                    return cardId.equals(transaction.getSenderCard().getId()) ||
                           cardId.equals(transaction.getReceiverCard().getId());
                if (transaction.getSenderCard() != null)
                    return cardId.equals(transaction.getSenderCard().getId());
                if (transaction.getReceiverCard() != null)
                    return cardId.equals(transaction.getReceiverCard().getId());
                return false;
                });
        if (type != null)
            transactions = transactions.filter(transaction -> transaction.getType().equals(type));
        if (start != null && end != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            String field = "start";
            try {
                LocalDateTime startDate = LocalDate.parse(start, formatter).atStartOfDay();
                field = "end";
                LocalDateTime endDate = LocalDate.parse(end, formatter).atStartOfDay();
                transactions = transactions.filter(transaction -> transaction.getTimestamp().isAfter(startDate) &&
                        transaction.getTimestamp().isBefore(endDate));
            }
            catch (DateTimeParseException ex) {
                throw new RuntimeException("Некорректное значение параметра '%s'".formatted(field));
            }
        }
        return transactions.toList();
    }
}

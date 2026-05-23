package com.banking.Banking.Service;

import com.banking.Banking.Configuration.QuerySpec;
import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.annotation.Nullable;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Service;

import javax.swing.text.html.parser.Entity;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    @Autowired
    private TransactionMapper mapper;
    private final int PAGE_SIZE = 5;

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

    public Transaction findById(Long transactionId, Authentication auth) throws AccessDeniedException {
        var transaction = repository.findById(transactionId).orElseThrow(() -> new EntityNotFoundException("Операция не найдена"));
        var client = clientService.findByUsername(auth.getName());
        if (client == null)
            throw new RuntimeException("Пользователь не найден");
        if (client.getId() != transaction.getReceiverCard().getClient().getId() &&
            client.getId() != transaction.getSenderCard().getClient().getId())
            throw new AccessDeniedException("Операция не принадлежит пользователю");
        return transaction;
    }

    public Page<Transaction> findTransactions(Long clientId, int pageNum) {
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "timestamp"));
        clientService.findByIdOrThrow(clientId);
        List<Long> cards = cardService.findByClientId(clientId).stream()
                .map(Card::getId)
                .toList();
        return repository.findAll(Specification.where(QuerySpec.belongsInCards(cards)), pageable);
    }

    public Page<Transaction> findTransactions(Long clientId, int pageNum, @Nullable OperationTypes type,
                  @Nullable Long cardId, @Nullable String start, @Nullable String end) throws AccessDeniedException {
        clientService.findByIdOrThrow(clientId);
        if (cardId != null && !cardService.belongsToClient(clientId, cardId))
            throw new AccessDeniedException("Доступ к карте запрещен");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String field = "start";
        LocalDateTime startDate;
        LocalDateTime endDate;
        try {
            startDate = start != null ? LocalDate.parse(start, formatter).atStartOfDay() : null;
            field = "end";
            endDate = end != null ? LocalDate.parse(end, formatter).atTime(LocalTime.MAX) : null;
        }
        catch (DateTimeParseException ex) {
            throw new RuntimeException("Некорректное значение параметра '%s'".formatted(field));
        }

        List<Long> cards = cardService.findByClientId(clientId).stream()
                .map(Card::getId)
                .toList();
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<Transaction> spec = Specification.unrestricted();
        if (!cards.isEmpty()) spec = spec.and(QuerySpec.belongsInCards(cards));
        if (type != null) spec = spec.and(QuerySpec.hasType(type));
        if (cardId != null) spec = spec.and(QuerySpec.belongsToCard(cardId));
        if (start != null && end != null) spec = spec.and(QuerySpec.timestampBetween(startDate, endDate));
        return repository.findAll(spec, pageable);
    }
}
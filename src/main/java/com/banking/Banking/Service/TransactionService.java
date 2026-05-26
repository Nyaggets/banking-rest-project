package com.banking.Banking.Service;

import com.banking.Banking.Configuration.QuerySpec;
import com.banking.Banking.Dto.CardStatsDto;
import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
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
import org.springframework.stereotype.Service;

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
        validationService.validateOperation(OperationTypes.TRANSFER_OUT, transactionDto);

        UUID uuid = UUID.randomUUID();
        Card senderCard = cardService.findById(transactionDto.getSenderCardId());
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        boolean isInternal = senderCard.getClient().getId() == receiverCard.getClient().getId();
        BigDecimal commission = calculateCommission(transactionDto.getAmount());
        Transaction transactionOut = Transaction.builder()
                .type(OperationTypes.TRANSFER_OUT)
                .isInternal(isInternal)
                .transferId(uuid)
                .clientCard(senderCard)
                .counterPartyName(receiverCard.getClientName())
                .counterPartyHiddenNumber(receiverCard.getLast4())
                .amount(transactionDto.getAmount())
                .commission(commission)
                .totalAmount(transactionDto.getAmount().add(commission))
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        Transaction transactionIn = Transaction.builder()
                .type(OperationTypes.TRANSFER_IN)
                .isInternal(isInternal)
                .transferId(uuid)
                .clientCard(receiverCard)
                .counterPartyName(senderCard.getClientName())
                .counterPartyHiddenNumber(senderCard.getLast4())
                .amount(transactionDto.getAmount())
                .totalAmount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = senderCard.getBalance();
        senderCard.setBalance(senderBalance.subtract(transactionOut.getTotalAmount()));
        receiverCard.setBalance(receiverCard.getBalance().add(transactionIn.getAmount()));
        repository.save(transactionIn);
        return repository.save(transactionOut);
    }

    public Transaction createWithdrawal(TransactionDtoRequest transactionDto) {
        validationService.validateOperation(OperationTypes.WITHDRAWAL, transactionDto);

        Card senderCard = cardService.findById(transactionDto.getSenderCardId());
        BigDecimal commission = calculateCommission(transactionDto.getAmount());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.WITHDRAWAL)
                .isInternal(false)
                .clientCard(senderCard)
                .counterPartyName(transactionDto.getCounterParty())
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
                .isInternal(false)
                .clientCard(receiverCard)
                .counterPartyName(transactionDto.getCounterParty())
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
        if (client.getId() != transaction.getClientCard().getClient().getId())
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

    public Page<Transaction> findTransactions(Long clientId, int pageNum, @Nullable List<OperationTypes> types,
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
        if (types != null) spec = spec.and(QuerySpec.hasType(types));
        if (cardId != null) spec = spec.and(QuerySpec.belongsToCard(cardId));
        if (start != null && end != null) spec = spec.and(QuerySpec.timestampBetween(startDate, endDate));
        return repository.findAll(spec, pageable);
    }

    public CardStatsDto getMonthlyStats(Long cardId, Long clientId) throws AccessDeniedException {
        clientService.findByIdOrThrow(clientId);
        if (!cardService.belongsToClient(clientId, cardId))
            throw new AccessDeniedException("Доступ к карте запрещен");
        LocalDate start = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1);
        LocalDate end = start.plusMonths(1);
        var transactions = repository.findAll().stream().filter(tr ->
                tr.getTimestamp().isAfter(start.atStartOfDay()) && tr.getTimestamp().isBefore(end.atTime(LocalTime.MAX)) && !tr.getIsInternal()).toList();
        CardStatsDto stats = new CardStatsDto(BigDecimal.ZERO, BigDecimal.ZERO);
        transactions.forEach(tr -> {
            if (tr.getType().equals(OperationTypes.TRANSFER_IN) || tr.getType().equals(OperationTypes.DEPOSIT))
                stats.setIncome(stats.getIncome().add(tr.getTotalAmount()));
            else if (tr.getType().equals(OperationTypes.TRANSFER_OUT) || tr.getType().equals(OperationTypes.WITHDRAWAL))
                stats.setOutcome(stats.getOutcome().add(tr.getTotalAmount()));
        });
        return stats;
    }
}
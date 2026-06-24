package com.banking.Banking.Service;

import com.banking.Banking.Configuration.QuerySpec;
import com.banking.Banking.Dto.CardStatsDto;
import com.banking.Banking.Dto.DepositDtoRequest;
import com.banking.Banking.Dto.TransferDtoRequest;
import com.banking.Banking.Dto.WithdrawalDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.CounterpartyTypeEnum;
import com.banking.Banking.Entity.OperationTypeEnum;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import com.banking.Banking.validation.CustomNotFoundException;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@Transactional
public class TransactionService {
    private TransactionRepository repository;
    private CardService cardService;
    private ClientService clientService;
    private TransactionValidationService validationService;
    private PhoneService phoneService;
    private final int PAGE_SIZE = 5;

    public TransactionService(TransactionRepository repository, CardService cardService, ClientService clientService,
                              TransactionValidationService validationService, PhoneService phoneService) {
        this.repository = repository;
        this.cardService = cardService;
        this.clientService = clientService;
        this.validationService = validationService;
        this.phoneService = phoneService;
    }

    public BigDecimal calculateCommission(BigDecimal amount, OperationTypeEnum type) {
        return switch (type) {
            case DEPOSIT, WITHDRAWAL -> BigDecimal.ZERO;
            case TRANSFER_OUT, TRANSFER_IN ->
                    amount.compareTo(new BigDecimal("100000")) >= 0
                            ? amount.multiply(new BigDecimal("0.05"))
                            : BigDecimal.ZERO;
        };
    }

    public Transaction createTransferToInternalClient(Long currentClientId, TransferDtoRequest transactionDto) {
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getClientCardId(), "sender");
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getCounterpartyCardIdentifier());
        if (receiverCard == null)
            throw new CustomNotFoundException("Карта получателя не найдена", "receiver");
        BigDecimal commission = calculateCommission(transactionDto.getAmount(), OperationTypeEnum.TRANSFER_OUT);
        BigDecimal totalAmount = transactionDto.getAmount().add(commission);

        validationService.transferValidation(currentClientId, transactionDto, senderCard, totalAmount);

        boolean isInternal = senderCard.getClient().getId().equals(receiverCard.getClient().getId());
        UUID uuid = UUID.randomUUID();
        Transaction transactionOut = Transaction.builder()
                .operationType(OperationTypeEnum.TRANSFER_OUT)
                .isInternal(isInternal)
                .transferId(uuid)
                .clientCard(senderCard)
                .counterpartyName(receiverCard.getClientName())
                .counterpartyIdentifier(receiverCard.getLast4())
                .counterpartyType(CounterpartyTypeEnum.CLIENT)
                .amount(transactionDto.getAmount())
                .commission(commission)
                .totalAmount(totalAmount)
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        Transaction transactionIn = Transaction.builder()
                .operationType(OperationTypeEnum.TRANSFER_IN)
                .counterpartyType(CounterpartyTypeEnum.CLIENT)
                .isInternal(isInternal)
                .transferId(uuid)
                .clientCard(receiverCard)
                .counterpartyName(senderCard.getClientName())
                .counterpartyIdentifier(senderCard.getLast4())
                .amount(transactionDto.getAmount())
                .totalAmount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        senderCard.setBalance(senderCard.getBalance().subtract(totalAmount));
        receiverCard.setBalance(receiverCard.getBalance().add(transactionIn.getAmount()));

        repository.save(transactionIn);
        return repository.save(transactionOut);
    }

    public Transaction createDeposit(DepositDtoRequest transactionDto, CounterpartyTypeEnum counterpartyType) {
        validationService.depositValidation(transactionDto);

        Card receiverCard = cardService.findByIdOrThrow(transactionDto.getClientCardId(), "receiver");
        Transaction transaction = Transaction.builder()
                .operationType(OperationTypeEnum.DEPOSIT)
                .isInternal(false)
                .clientCard(receiverCard)
                .counterpartyName(transactionDto.getCounterpartyIdentifier())
                .counterpartyType(counterpartyType)
                .amount(transactionDto.getAmount())
                .totalAmount(transactionDto.getAmount())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = receiverCard.getBalance();
        receiverCard.setBalance(senderBalance.add(transaction.getTotalAmount()));
        return repository.save(transaction);
    }

    public Transaction createWithdrawal(Long currentClientId, WithdrawalDtoRequest dtoRequest,
                                        CounterpartyTypeEnum counterpartyType) {
        Card senderCard = cardService.findByIdOrThrow(dtoRequest.getClientCardId(), "sender");

        validationService.withdrawalValidation(currentClientId, senderCard, dtoRequest.getAmount());

        BigDecimal commission = calculateCommission(dtoRequest.getAmount(), OperationTypeEnum.WITHDRAWAL);
        Transaction transaction = Transaction.builder()
                .operationType(OperationTypeEnum.WITHDRAWAL)
                .isInternal(false)
                .clientCard(senderCard)
                .counterpartyName(dtoRequest.getCounterpartyName())
                .counterpartyIdentifier(dtoRequest.getCounterpartyIdentifier())
                .counterpartyType(counterpartyType) 
                .amount(dtoRequest.getAmount())
                .commission(commission)
                .totalAmount(dtoRequest.getAmount().add(commission))
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal cardBalance = senderCard.getBalance();
        senderCard.setBalance(cardBalance.subtract(transaction.getTotalAmount()));
        return repository.save(transaction);
    }

    public Transaction createBalanceTopUp(Long currentClientId, WithdrawalDtoRequest withdrawalDto) {
        var receiverOperator = phoneService.getOperatorOrThrow(withdrawalDto.getCounterpartyIdentifier());
        WithdrawalDtoRequest topUpDto = WithdrawalDtoRequest.builder()
                .clientCardId(withdrawalDto.getClientCardId())
                .amount(withdrawalDto.getAmount())
                .counterpartyIdentifier(withdrawalDto.getCounterpartyIdentifier())
                .counterpartyName(receiverOperator.getOperatorName())
                .build();
        return createWithdrawal(currentClientId, topUpDto, CounterpartyTypeEnum.MERCHANT);
    }

    public Transaction findById(Long transactionId, Authentication auth) throws AccessDeniedException {
        var transaction = repository.findById(transactionId).orElseThrow(() -> new CustomNotFoundException("Операция не найдена", "transaction"));
        var client = clientService.findByLoginOrThrow(auth.getName());
        if (client.getId() != transaction.getClientCard().getClient().getId())
            throw new AccessDeniedException("Операция не принадлежит пользователю");
        return transaction;
    }

    public Page<Transaction> findTransactions(Long clientId, int pageNum, @Nullable List<OperationTypeEnum> types,
              @Nullable Long cardId, @Nullable LocalDate start, @Nullable LocalDate end) throws AccessDeniedException {
        clientService.findByIdOrThrow(clientId);
        List<Long> cards = cardService.findByClientId(clientId).stream()
                .map(Card::getId)
                .toList();
        Pageable pageable = PageRequest.of(pageNum, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "timestamp"));
        Specification<Transaction> spec = Specification.unrestricted();
        if (!cards.isEmpty())
            spec = spec.and(QuerySpec.belongsInCards(cards));
        if (types != null)
            spec = spec.and(QuerySpec.hasType(types));
        if (start != null && end != null) {
            LocalDateTime startDate = start.atStartOfDay();
            LocalDateTime endDate = end.atTime(LocalTime.MAX);
            spec = spec.and(QuerySpec.timestampBetween(startDate, endDate));
        }
        if (cardId != null) {
            Card card = cardService.findByIdOrThrow(cardId, "card");
            cardService.belongsToClientOrThrow(clientId, card);
            spec = spec.and(QuerySpec.belongsToCard(cardId));
        }
        else
            spec = spec.and(QuerySpec.removeTransferDuplicates());
        return repository.findAll(spec, pageable);
    }

    public CardStatsDto getMonthlyStats(Long cardId, Long clientId) throws AccessDeniedException {
        clientService.findByIdOrThrow(clientId);
        Card card = cardService.findByIdOrThrow(cardId, "card");
        cardService.belongsToClientOrThrow(clientId, card);

        LocalDate start = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonthValue(), 1);
        LocalDate end = start.plusMonths(1);
        var transactions = repository.findAll().stream()
                .filter(tr -> tr.getClientCard().getId() == cardId &&
                                        tr.getTimestamp().isAfter(start.atStartOfDay()) &&
                                        tr.getTimestamp().isBefore(end.atTime(LocalTime.MAX)) &&
                                        !tr.getIsInternal())
                .toList();

        CardStatsDto stats = new CardStatsDto(BigDecimal.ZERO, BigDecimal.ZERO);
        transactions.forEach(tr -> {
            if (tr.getOperationType().equals(OperationTypeEnum.TRANSFER_IN) || tr.getOperationType().equals(OperationTypeEnum.DEPOSIT))
                stats.setIncome(stats.getIncome().add(tr.getTotalAmount()));
            else if (tr.getOperationType().equals(OperationTypeEnum.TRANSFER_OUT) || tr.getOperationType().equals(OperationTypeEnum.WITHDRAWAL))
                stats.setOutcome(stats.getOutcome().add(tr.getTotalAmount()));
        });
        return stats;
    }
}
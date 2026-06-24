package com.banking.Banking.Service;

import com.banking.Banking.Dto.DepositDtoRequest;
import com.banking.Banking.Dto.TransferDtoRequest;
import com.banking.Banking.Dto.WithdrawalDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.CounterpartyTypeEnum;
import com.banking.Banking.Entity.MobileOperatorEnum;
import com.banking.Banking.Entity.OperationTypeEnum;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import com.banking.Banking.Dto.CardStatsDto;
import com.banking.Banking.validation.CustomException;
import com.banking.Banking.validation.CustomNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository repository;
    @Mock
    private CardService cardService;
    @Mock
    private ClientService clientService;
    @Mock
    private TransactionValidationService validationService;
    @Mock
    private PhoneService phoneService;

    @InjectMocks
    private TransactionService transactionService;

    private Client client;
    private Card senderCard;
    private Card receiverCard;
    private TransferDtoRequest transferDto;
    private DepositDtoRequest depositDto;
    private WithdrawalDtoRequest withdrawalDto;

    @BeforeEach
    void setUp() {
        client = Client.builder().id(1L).login("user").build();

        senderCard = Card.builder()
                .id(10L).client(client).balance(new BigDecimal("100"))
                .last4("1111").clientName("Ivan Ivanov").build();

        receiverCard = Card.builder()
                .id(20L).client(new Client()).balance(BigDecimal.ZERO)
                .last4("2222").clientName("Petr Petrov").build();

        transferDto = TransferDtoRequest.builder()
                .clientCardId(10L).counterpartyCardIdentifier("2222")
                .amount(new BigDecimal("100")).build();

        depositDto = DepositDtoRequest.builder()
                .clientCardId(20L).counterpartyIdentifier("SOURCE")
                .amount(new BigDecimal("100")).build();

        withdrawalDto = WithdrawalDtoRequest.builder()
                .clientCardId(10L).counterpartyIdentifier("MERCHANT")
                .amount(new BigDecimal("100")).build();
    }

    @Test
    void calculateCommission_Success_TransferLowAmount() {
        assertThat(transactionService.calculateCommission(new BigDecimal("50000"), OperationTypeEnum.TRANSFER_OUT))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void calculateCommission_Success_TransferHighAmount() {
        assertThat(transactionService.calculateCommission(new BigDecimal("200000"), OperationTypeEnum.TRANSFER_IN))
                .isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    void createTransferToInternalClient_Success() {
        when(cardService.findByIdOrThrow(10L, "sender")).thenReturn(senderCard);
        when(cardService.findByCardIdentifier("2222")).thenReturn(receiverCard);
        doNothing().when(validationService).transferValidation(anyLong(), any(), any(), any());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createTransferToInternalClient(1L, transferDto);

        assertNotNull(result);
        assertThat(result.getOperationType()).isEqualTo(OperationTypeEnum.TRANSFER_OUT);
        assertThat(senderCard.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(receiverCard.getBalance()).isEqualByComparingTo(new BigDecimal("100"));
        verify(repository, times(2)).save(any());
    }

    @Test
    void createTransferToInternalClient_ValidationError() {
        assertThrows(CustomNotFoundException.class, () -> transactionService.createTransferToInternalClient(1L, transferDto));
    }

    @Test
    void createDeposit_Success() {
        doNothing().when(validationService).depositValidation(any());
        when(cardService.findByIdOrThrow(20L, "receiver")).thenReturn(receiverCard);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createDeposit(depositDto, CounterpartyTypeEnum.SALARY);

        assertNotNull(result);
        assertThat(result.getOperationType()).isEqualTo(OperationTypeEnum.DEPOSIT);
        assertThat(receiverCard.getBalance()).isEqualByComparingTo(new BigDecimal("100"));
        verify(repository, times(1)).save(any());
    }

    @Test
    void createDeposit_CardNotFound() {
        doNothing().when(validationService).depositValidation(any());
        when(cardService.findByIdOrThrow(anyLong(), anyString())).thenReturn(null);

        assertThrows(NullPointerException.class, () -> transactionService.createDeposit(depositDto, CounterpartyTypeEnum.SALARY));
    }

    @Test
    void createWithdrawal_Success() {
        when(cardService.findByIdOrThrow(10L, "sender")).thenReturn(senderCard);
        doNothing().when(validationService).withdrawalValidation(anyLong(), any(), any());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Transaction result = transactionService.createWithdrawal(1L, withdrawalDto, CounterpartyTypeEnum.MERCHANT);

        assertNotNull(result);
        assertThat(result.getOperationType()).isEqualTo(OperationTypeEnum.WITHDRAWAL);
        assertThat(senderCard.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void createWithdrawal_AccessDenied() {
        doThrow(new AccessDeniedException("Доступ запрещен")).when(validationService).withdrawalValidation(anyLong(), any(), any());

        assertThrows(AccessDeniedException.class, () -> transactionService.createWithdrawal(1L, withdrawalDto, CounterpartyTypeEnum.MERCHANT));
    }

    @Test
    void createBalanceTopUp_Success() {
        when(phoneService.getOperatorOrThrow("+79001234567")).thenReturn(MobileOperatorEnum.MTS);
        doNothing().when(validationService).withdrawalValidation(anyLong(), any(), any());
        when(cardService.findByIdOrThrow(anyLong(), anyString())).thenReturn(senderCard);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        WithdrawalDtoRequest topUpDto = WithdrawalDtoRequest.builder()
                .clientCardId(10L).counterpartyIdentifier("+79001234567").amount(new BigDecimal("100")).build();

        Transaction result = transactionService.createBalanceTopUp(1L, topUpDto);

        assertNotNull(result);
        verify(phoneService).getOperatorOrThrow("+79001234567");
    }

    @Test
    void createBalanceTopUp_OperatorNotFound() {
        when(phoneService.getOperatorOrThrow("invalid")).thenThrow(new RuntimeException("Оператор не найден"));

        WithdrawalDtoRequest topUpDto = WithdrawalDtoRequest.builder()
                .clientCardId(10L).counterpartyIdentifier("invalid").amount(new BigDecimal("100")).build();

        assertThrows(RuntimeException.class, () -> transactionService.createBalanceTopUp(1L, topUpDto));
    }

    @Test
    void findTransactions_Success() {
        when(clientService.findByIdOrThrow(1L)).thenReturn(client);
        when(cardService.findByIdOrThrow(10L, "card")).thenReturn(senderCard);
        doNothing().when(cardService).belongsToClientOrThrow(1L, senderCard);
        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard));
        when(repository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Transaction())));

        Page<Transaction> result = transactionService.findTransactions(1L, 0, null, 10L, null, null);

        assertThat(result.getContent()).hasSize(1);
        verify(clientService).findByIdOrThrow(1L);
    }

    @Test
    void findTransactions_AccessDenied() {
        when(clientService.findByIdOrThrow(1L)).thenReturn(client);
        when(cardService.findByIdOrThrow(10L, "card")).thenReturn(senderCard);
        doThrow(new AccessDeniedException("Доступ к карте запрещен")).when(cardService).belongsToClientOrThrow(1L, senderCard);

        assertThrows(AccessDeniedException.class, () -> transactionService.findTransactions(1L, 0, null, 10L, null, null));
    }

    @Test
    void getMonthlyStats_Success() {
        when(clientService.findByIdOrThrow(1L)).thenReturn(client);
        when(cardService.findByIdOrThrow(10L, "card")).thenReturn(senderCard);
        doNothing().when(cardService).belongsToClientOrThrow(1L, senderCard);

        Transaction income = Transaction.builder()
                .operationType(OperationTypeEnum.DEPOSIT).totalAmount(new BigDecimal("100"))
                .clientCard(senderCard).isInternal(false).timestamp(LocalDateTime.now()).build();
        Transaction outcome = Transaction.builder()
                .operationType(OperationTypeEnum.WITHDRAWAL).totalAmount(new BigDecimal("200"))
                .clientCard(senderCard).isInternal(false).timestamp(LocalDateTime.now()).build();
        when(repository.findAll()).thenReturn(List.of(income, outcome));

        CardStatsDto stats = transactionService.getMonthlyStats(10L, 1L);

        assertThat(stats.getIncome()).isEqualByComparingTo(new BigDecimal("100"));
        assertThat(stats.getOutcome()).isEqualByComparingTo(new BigDecimal("200"));
    }

    @Test
    void getMonthlyStats_ForbiddenCard() {
        when(clientService.findByIdOrThrow(1L)).thenReturn(client);
        when(cardService.findByIdOrThrow(20L, "card")).thenReturn(receiverCard);
        doThrow(new AccessDeniedException("Доступ к карте запрещен")).when(cardService).belongsToClientOrThrow(1L, receiverCard);

        assertThrows(AccessDeniedException.class, () -> transactionService.getMonthlyStats(20L, 1L));
    }
}
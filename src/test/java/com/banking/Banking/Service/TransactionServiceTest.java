package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Autowired
    private Validator validator;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardService cardService;
    @Mock
    private ClientService clientService;
    @Mock
    private TransactionValidationService validationService;
    @Mock
    private TransactionMapper mapper;
    @InjectMocks
    private TransactionService transactionService;

    private Card senderCard;
    private Card receiverCard;
    private TransactionDtoRequest transferDto;
    private TransactionDtoRequest depositDto;
    private TransactionDtoRequest withdrawalDto;
    private Transaction transferSender;
    private Transaction transferReceiver;
    private Transaction depositSender;
    private Transaction depositReceiver;
    private Transaction withdrawalSender;
    private Transaction withdrawalReceiver;
    private Transaction timestampDepositMinus1;
    private Transaction timestampDepositMinus2;
    private List<Transaction> transactions;
    private LocalDateTime fixedDate;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        Client senderClient = new Client();
        senderClient.setId(1L);

        senderCard = Card.builder()
                .id(1L)
                .client(senderClient)
                .balance(new BigDecimal("1000"))
                .cardNumber("1111")
                .build();
        receiverCard = Card.builder()
                .id(2L)
                .client(new Client())
                .balance(new BigDecimal("500"))
                .cardNumber("2222")
                .build();

        transferDto = TransactionDtoRequest.builder()
                .senderCardId(1L)
                .receiverIdentifier("2222")
                .amount(new BigDecimal("100"))
                .build();
        depositDto = TransactionDtoRequest.builder()
                .counterParty("source")
                .receiverIdentifier("2222")
                .amount(new BigDecimal("100"))
                .build();
        withdrawalDto = TransactionDtoRequest.builder()
                .senderCardId(1L)
                .counterParty("merchant")
                .amount(new BigDecimal("100"))
                .build();

        fixedDate = LocalDateTime.parse("2026-06-06 10:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        transferSender = Transaction.builder()
                .id(1L)
                .clientCard(senderCard)
                .timestamp(fixedDate)
                .type(OperationTypes.TRANSFER_OUT)
                .build();
        transferReceiver = Transaction.builder()
                .id(2L)
                .clientCard(receiverCard)
                .timestamp(fixedDate)
                .type(OperationTypes.TRANSFER_IN)
                .build();
        withdrawalSender = Transaction.builder()
                .id(3L)
                .clientCard(senderCard)
                .timestamp(fixedDate)
                .type(OperationTypes.WITHDRAWAL)
                .build();
        withdrawalReceiver = Transaction.builder()
                .id(4L)
                .clientCard(receiverCard)
                .timestamp(fixedDate)
                .type(OperationTypes.WITHDRAWAL)
                .build();
        depositSender = Transaction.builder()
                .id(5L)
                .clientCard(senderCard)
                .timestamp(fixedDate)
                .type(OperationTypes.DEPOSIT)
                .build();
        depositReceiver = Transaction.builder()
                .id(6L)
                .clientCard(receiverCard)
                .timestamp(fixedDate)
                .type(OperationTypes.DEPOSIT)
                .build();
        timestampDepositMinus1 = Transaction.builder()
                .id(7L)
                .type(OperationTypes.DEPOSIT)
                .timestamp(fixedDate.minusDays(1))
                .build();
        timestampDepositMinus2 = Transaction.builder()
                .id(8L)
                .type(OperationTypes.DEPOSIT)
                .timestamp(fixedDate.minusDays(2))
                .build();
        transactions = List.of(transferSender, transferReceiver, depositSender, depositReceiver,
                withdrawalSender, withdrawalReceiver, timestampDepositMinus1, timestampDepositMinus2);
        pageable = PageRequest.of(0, 2);
    }

    @Test
    void findByCardNumber() {
        List<Transaction> transactions = List.of(
                new Transaction(),
                new Transaction());
        when(transactionRepository.findByCardId(anyLong())).thenReturn(transactions);

        List<Transaction> result = transactionService.findByCardId(1L);

        assertThat(result).hasSize(2);
    }

    @Test
    void findTransactions_SuccessByClientId() {
        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard));

        var clientTransactions = transactionService.findTransactions(1L, 0);

        assertThat(clientTransactions).isNotNull();
        verify(cardService).findByClientId(1L);
    }

    @Test
    void findTransactions_SuccessByClientIdCardId() throws AccessDeniedException {
        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard, receiverCard));
        when(cardService.belongsToClient(1L, 1L)).thenReturn(true);
        when(transactionRepository.findByCardId(1L)).thenReturn(transactions);

        var clientTransactions = transactionService
                .findTransactions(1L, 1, null, 1L, null, null);

        assertThat(clientTransactions.getContent().size()).isEqualTo(3);
    }

    @Test
    void findTransactions_SuccessByClientIdType() throws AccessDeniedException {
        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard, receiverCard));
        when(transactionRepository.findByCardId(1L)).thenReturn(transactions);

        var transferTransactions = transactionService
                .findTransactions(1L, 1, List.of(OperationTypes.TRANSFER_OUT), null, null, null);
        var depositTransactions = transactionService
                .findTransactions(1L, 1, List.of(OperationTypes.DEPOSIT),null,  null, null);
        var withdrawalTransactions = transactionService
                .findTransactions(1L, 1, List.of(OperationTypes.WITHDRAWAL), null, null, null);

        assertThat(transferTransactions.getContent()).contains(transferSender, transferReceiver);
        assertThat(depositTransactions).contains(depositSender, depositReceiver, timestampDepositMinus1, timestampDepositMinus2);
        assertThat(withdrawalTransactions).contains(withdrawalSender, withdrawalReceiver);
    }

    @Test
    void findTransactions_SuccessByClientIdPeriod() throws AccessDeniedException {
        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard, receiverCard));
        when(transactionRepository.findByCardId(1L)).thenReturn(transactions);

        LocalDate timestamp = fixedDate.toLocalDate();
        List<Transaction> clientTransactions = (List<Transaction>) transactionService
                .findTransactions(1L, 0, null, null, timestamp.minusDays(5).toString(), timestamp.toString());

        assertThat(clientTransactions).contains(timestampDepositMinus1, timestampDepositMinus2);
        assertThat(clientTransactions).hasSize(2);
    }

//    @Test
//    void findTransactions_SliceLol() {
//        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard, receiverCard));
//        when(transactionRepository.findAll(, any())).thenReturn(new SliceImpl<>(transactions, pageable, false));
//        when(mapper.toDtoList(anyList())).thenReturn(List.of(new TransactionDtoResponse(), new TransactionDtoResponse()));
//
//        Slice<TransactionDtoResponse> sliceResult = transactionService.findTransactions(1L, 0);
//
//        assertThat(sliceResult.getContent()).hasSize(2);
//        assertThat(sliceResult.getPageable().getPageSize()).isEqualTo(2);
//        assertThat(sliceResult.getPageable().getOffset()).isEqualTo(0);
//    }

    @Test
    void createTransfer_Success() {
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transfer = transactionService.createTransfer(transferDto);

        assertThat(transfer).isNotNull();
        assertThat(transfer.getType()).isEqualTo(OperationTypes.TRANSFER_OUT);
        assertThat(senderCard.getBalance()).isEqualByComparingTo(new BigDecimal("900"));
        assertThat(receiverCard.getBalance()).isEqualByComparingTo(new BigDecimal("600"));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createDeposit_Success() {
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transfer = transactionService.createDeposit(depositDto);

        assertThat(transfer).isNotNull();
        assertThat(transfer.getType()).isEqualTo(OperationTypes.DEPOSIT);
        assertThat(receiverCard.getBalance()).isEqualByComparingTo(new BigDecimal("600"));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void createWithdrawal_Success() {
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transfer = transactionService.createWithdrawal(withdrawalDto);

        assertThat(transfer).isNotNull();
        assertThat(transfer.getType()).isEqualTo(OperationTypes.WITHDRAWAL);
        assertThat(senderCard.getBalance()).isEqualByComparingTo(new BigDecimal("900"));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}

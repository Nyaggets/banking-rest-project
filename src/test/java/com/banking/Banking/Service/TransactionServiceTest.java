package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
public class TransactionServiceTest {
    @Autowired
    private Validator validator;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CardService cardService;
    @Mock
    private ClientService clientService;
    @InjectMocks
    private TransactionService transactionService;

    private Card senderCard;
    private Card receiverCard;
    private Client senderClient;
    private TransactionDtoRequest transferDto;
    private TransactionDtoRequest depositDto;
    private TransactionDtoRequest withdrawalDto;

    @BeforeEach
    void setUp() {
        senderClient = new Client();
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
                .source("source")
                .receiverIdentifier("2222")
                .amount(new BigDecimal("100"))
                .build();

        withdrawalDto = TransactionDtoRequest.builder()
                .senderCardId(1L)
                .merchant("merchant")
                .amount(new BigDecimal("100"))
                .build();
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
    void findByClientId_Success() {
        when(cardService.findByClientId(1L)).thenReturn(List.of(senderCard));
        when(transactionRepository.findByCardId(1L)).thenReturn(List.of());

        List<Transaction> clientTransactions = transactionService.findByClientId(1L);

        assertThat(clientTransactions).isNotNull();
        verify(cardService).findByClientId(1L);
    }

    @Test
    @WithMockUser(username = "client")
    void transferValidation_Success() {
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var result = transactionService.transferValidation(transferDto);

        assertThat(result).isEmpty();
    }
    @Test
    @WithMockUser(username = "client")
    void transferValidation_Unauthorized() {
        senderCard.setClient(new Client());
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);
        when(clientService.findByUsername(anyString())).thenReturn(null);

        var result = transactionService.transferValidation(transferDto);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).containsKey("unauthorized");
    }
    @Test
    @WithMockUser(username = "client")
    void transferValidation_SeveralErrors() {
        transferDto.setAmount(BigDecimal.ZERO);
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var result = transactionService.transferValidation(transferDto);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsKey("amount");
        assertThat(result).containsKey("receiver");
    }
    @Test
    void createTransfer_Success() {
        when(cardService.findByIdOrThrow(1L)).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transfer = transactionService.createTransfer(transferDto);

        assertThat(transfer).isNotNull();
        assertThat(transfer.getType()).isEqualTo(OperationTypes.TRANSFER);
        assertThat(senderCard.getBalance()).isEqualByComparingTo(new BigDecimal("900"));
        assertThat(receiverCard.getBalance()).isEqualByComparingTo(new BigDecimal("600"));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void depositValidation_Success() {
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);

        var result = transactionService.depositValidation(depositDto);

        assertThat(result).isEmpty();
    }
    @Test
    void depositValidation_SeveralErrors() {
        depositDto.setAmount(BigDecimal.ZERO);

        var result = transactionService.depositValidation(depositDto);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsKey("amount");
        assertThat(result).containsKey("receiver");
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
    @WithMockUser(username = "client")
    void withdrawalValidation_Success() {
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var result = transactionService.withdrawalValidation(withdrawalDto);

        assertThat(result).isEmpty();
    }
    @Test
    @WithMockUser(username = "client")
    void withdrawalValidation_Forbidden() {
        senderCard.setClient(new Client());
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var result = transactionService.withdrawalValidation(withdrawalDto);

        assertThat(result.size()).isEqualTo(1);
        assertThat(result).containsKey("forbidden");
    }
    @Test
    @WithMockUser(username = "client")
    void withdrawalValidation_SeveralErrors() {
        withdrawalDto.setAmount(BigDecimal.ZERO);
        senderCard.setClient(new Client());
        when(cardService.findById(1L)).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var result = transactionService.withdrawalValidation(withdrawalDto);

        assertThat(result.size()).isEqualTo(2);
        assertThat(result).containsKey("amount");
        assertThat(result).containsKey("forbidden");
    }
    @Test
    void createWithdrawal_Success() {
        when(cardService.findByIdOrThrow(1L)).thenReturn(senderCard);
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transaction transfer = transactionService.createWithdrawal(withdrawalDto);

        assertThat(transfer).isNotNull();
        assertThat(transfer.getType()).isEqualTo(OperationTypes.WITHDRAWAL);
        assertThat(senderCard.getBalance()).isEqualByComparingTo(new BigDecimal("900"));

        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }
}

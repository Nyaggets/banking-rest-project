package com.banking.Banking.Service;

import com.banking.Banking.Dto.DepositDtoRequest;
import com.banking.Banking.Dto.TransferDtoRequest;
import com.banking.Banking.Dto.WithdrawalDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.validation.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionValidationServiceTest {

    @Mock private CardService cardService;
    @Mock private ClientService clientService;
    @Mock private VerifyIdentityService identityService;

    @InjectMocks
    private TransactionValidationService validationService;

    private Client client;
    private Card senderCard;
    private Card receiverCard;
    private TransferDtoRequest transferDto;
    private WithdrawalDtoRequest withdrawalDto;
    private DepositDtoRequest depositDto;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .login("user")
                .build();

        senderCard = Card.builder()
                .id(10L)
                .client(client)
                .balance(new BigDecimal("100"))
                .last4("1111")
                .clientName("Ivan Ivanov")
                .build();

        receiverCard = Card.builder()
                .id(20L)
                .client(new Client())
                .balance(BigDecimal.ZERO)
                .last4("2222")
                .clientName("Petr Petrov")
                .build();

        transferDto = TransferDtoRequest.builder()
                .clientCardId(1L)
                .counterpartyCardIdentifier("2222")
                .amount(new BigDecimal("100"))
                .build();

        withdrawalDto = WithdrawalDtoRequest.builder()
                .clientCardId(1L)
                .amount(new BigDecimal("100"))
                .build();

        depositDto = DepositDtoRequest.builder()
                .counterpartyIdentifier("2222")
                .build();
    }

    @Test
    void amountValidation_Success() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), anyString())).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);

        assertDoesNotThrow(() -> validationService.withdrawalValidation(1L, withdrawalDto));
    }

    @Test
    void amountValidation_OutOfRange() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), anyString())).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);

        withdrawalDto.setAmount(new BigDecimal("5"));

        CustomException exception = assertThrows(CustomException.class, () -> validationService.withdrawalValidation(1L, withdrawalDto));
        assertThat(exception.getErrors()).containsKey("amount");
    }

    @Test
    void clientSenderValidation_Success() {
        when(clientService.findByIdOrThrow(1L)).thenReturn(client);
        when(cardService.findByIdOrThrow(1L, "sender")).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);
        when(cardService.findByCardIdentifier("2222")).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.transferValidation(1L, transferDto));
    }

    @Test
    void clientSenderValidation_AccessDenied() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), eq("sender"))).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);
        senderCard.getClient().setId(99L);

        assertThrows(AccessDeniedException.class, () -> validationService.transferValidation(1L, transferDto));
    }

    @Test
    void receiverValidation_Success() {
        when(cardService.findByCardIdentifier("2222")).thenReturn(receiverCard);
        assertDoesNotThrow(() -> validationService.depositValidation(depositDto));
    }

    @Test
    void receiverValidation_NotFound() {
        when(cardService.findByCardIdentifier("2222")).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> validationService.depositValidation(depositDto));
        assertThat(exception.getErrors()).containsKey("receiver");
    }
    
    @Test
    void transferValidation_Success() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), eq("sender"))).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);
        when(cardService.findByCardIdentifier("2222")).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.transferValidation(1L, transferDto));
    }

    @Test
    void transferValidation_SenderEqualsReceiver() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), eq("sender"))).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);
        when(cardService.findByCardIdentifier("2222")).thenReturn(senderCard);

        CustomException exception = assertThrows(CustomException.class, () -> validationService.transferValidation(1L, transferDto));
        assertThat(exception.getErrors()).containsKey("receiver");
    }
    
    @Test
    void withdrawalValidation_Success() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), eq("sender"))).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);

        assertDoesNotThrow(() -> validationService.withdrawalValidation(1L, withdrawalDto));
    }

    @Test
    void withdrawalValidation_InsufficientFunds() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByIdOrThrow(anyLong(), eq("sender"))).thenReturn(senderCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);
        withdrawalDto.setAmount(new BigDecimal("1000"));

        CustomException exception = assertThrows(CustomException.class, () -> validationService.withdrawalValidation(1L, withdrawalDto));
        assertThat(exception.getErrors()).containsKey("amount");
    }
    
    @Test
    void depositValidation_Success() {
        when(cardService.findByCardIdentifier("2222")).thenReturn(receiverCard);
        assertDoesNotThrow(() -> validationService.depositValidation(depositDto));
    }

    @Test
    void depositValidation_ReceiverError() {
        when(cardService.findByCardIdentifier("2222")).thenReturn(null);

        CustomException exception = assertThrows(CustomException.class, () -> validationService.depositValidation(depositDto));
        assertThat(exception.getErrors()).containsKey("receiver");
    }
}
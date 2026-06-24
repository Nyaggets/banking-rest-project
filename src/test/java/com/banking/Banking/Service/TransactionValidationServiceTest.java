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
        client = Client.builder().id(1L).login("user").build();

        senderCard = Card.builder()
                .id(10L)
                .client(client)
                .balance(new BigDecimal("100"))
                .build();

        receiverCard = Card.builder()
                .id(20L)
                .client(new Client())
                .balance(BigDecimal.ZERO)
                .build();

        transferDto = TransferDtoRequest.builder()
                .clientCardId(10L)
                .counterpartyCardIdentifier("2222")
                .amount(new BigDecimal("50"))
                .build();

        withdrawalDto = WithdrawalDtoRequest.builder()
                .clientCardId(10L)
                .counterpartyIdentifier("counterparty")
                .amount(new BigDecimal("50"))
                .build();

        depositDto = DepositDtoRequest.builder()
                .clientCardId(20L)
                .counterpartyIdentifier("2222")
                .build();
    }

    @Test
    void transferValidation_Success() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByCardIdentifier("2222")).thenReturn(receiverCard);
        when(cardService.findById(anyLong())).thenReturn(senderCard);

        assertDoesNotThrow(() -> validationService.transferValidation(1L, transferDto, senderCard, new BigDecimal("50")));
    }

    @Test
    void transferValidation_AccessDenied() {
        senderCard.getClient().setId(99L);
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findById(anyLong())).thenReturn(senderCard);

        assertThrows(AccessDeniedException.class, () -> validationService.transferValidation(1L, transferDto, senderCard, new BigDecimal("50")));
    }

    @Test
    void transferValidation_SenderEqualsReceiver() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(senderCard);

        CustomException ex = assertThrows(CustomException.class, () -> validationService.transferValidation(1L, transferDto, senderCard, new BigDecimal("50")));
        assertThat(ex.getErrors()).containsKey("receiver");
    }

    @Test
    void transferValidation_ReceiverNotFound() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class, () -> validationService.transferValidation(1L, transferDto, senderCard, new BigDecimal("50")));
        assertThat(ex.getErrors()).containsKey("receiver");
    }

    @Test
    void withdrawalValidation_Success() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(cardService.findById(anyLong())).thenReturn(senderCard);

        assertDoesNotThrow(() -> validationService.withdrawalValidation(1L, senderCard, new BigDecimal("50")));
    }

    @Test
    void withdrawalValidation_InsufficientFunds() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);

        CustomException ex = assertThrows(CustomException.class, () -> validationService.withdrawalValidation(1L, senderCard, new BigDecimal("2000")));
        assertThat(ex.getErrors()).containsKey("amount");
    }

    @Test
    void withdrawalValidation_OutOfRange() {
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);

        withdrawalDto.setAmount(new BigDecimal("5"));
        CustomException ex = assertThrows(CustomException.class, () -> validationService.withdrawalValidation(1L, senderCard, new BigDecimal("5")));
        assertThat(ex.getErrors()).containsKey("amount");
    }

    @Test
    void depositValidation_Success() {
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.depositValidation(depositDto));
    }

    @Test
    void depositValidation_ReceiverError() {
        when(cardService.findByCardIdentifier(anyString())).thenReturn(null);
        CustomException ex = assertThrows(CustomException.class, () -> validationService.depositValidation(depositDto));
        assertThat(ex.getErrors()).containsKey("receiver");
    }
}
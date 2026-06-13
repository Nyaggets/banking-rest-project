package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.OperationTypeEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.annotation.SecurityTestExecutionListeners;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
@SecurityTestExecutionListeners
public class TransactionValidationServiceTest {
    private static final String CLIENT_LOGIN = "client";
    private static final String RECEIVER_LAST4 = "2222";
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("100");

    @Mock
    private CardService cardService;
    @Mock
    private ClientService clientService;
    @InjectMocks
    private TransactionValidationService validationService;

    private Card senderCard;
    private Card receiverCard;
    private Client senderClient;
    private TransactionDtoRequest transferDto;
    private TransactionDtoRequest depositDto;
    private TransactionDtoRequest withdrawalDto;

    @BeforeEach
    void setUp() {
        senderClient = Client.builder()
                .id(1L)
                .login(CLIENT_LOGIN)
                .build();

        Client receiverClient = Client.builder()
                .id(2L)
                .build();

        senderCard = Card.builder()
                .id(1L)
                .client(senderClient)
                .balance(new BigDecimal("1000"))
                .last4("1111")
                .build();

        receiverCard = Card.builder()
                .id(2L)
                .client(receiverClient)
                .balance(new BigDecimal("500"))
                .last4(RECEIVER_LAST4)
                .build();

        transferDto = TransactionDtoRequest.builder()
                .clientCardId(senderCard.getId())
                .receiverIdentifier(RECEIVER_LAST4)
                .amount(VALID_AMOUNT)
                .build();

        depositDto = TransactionDtoRequest.builder()
                .counterParty("source")
                .receiverIdentifier(RECEIVER_LAST4)
                .amount(VALID_AMOUNT)
                .build();

        withdrawalDto = TransactionDtoRequest.builder()
                .clientCardId(senderCard.getId())
                .counterParty("merchant")
                .amount(VALID_AMOUNT)
                .build();
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN)
    void transferValidation_Success() {
        when(clientService.findByLogin(CLIENT_LOGIN)).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId(), "sender")).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(RECEIVER_LAST4)).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.validateOperation(OperationTypeEnum.TRANSFER_OUT, transferDto));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN)
    void transferValidation_Unauthorized() {
        when(clientService.findByLogin(CLIENT_LOGIN)).thenReturn(null);

        assertThrows(BadCredentialsException.class,
                () -> validationService.validateOperation(OperationTypeEnum.TRANSFER_OUT, transferDto));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN)
    void transferValidation_ReceiverAmountErrors() {
        transferDto.setAmount(BigDecimal.ZERO);
        when(clientService.findByLogin(CLIENT_LOGIN)).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId(), "sender")).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(RECEIVER_LAST4)).thenReturn(senderCard);

        var errors = assertThrows(MultipleValidationException.class,
                () -> validationService.validateOperation(OperationTypeEnum.TRANSFER_OUT, transferDto));
        assertTrue(errors.getErrors().containsKey("receiver"));
        assertTrue(errors.getErrors().containsKey("amount"));
    }

    @Test
    void depositValidation_Success() {
        when(cardService.findByCardIdentifier(RECEIVER_LAST4)).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.validateOperation(OperationTypeEnum.DEPOSIT, depositDto));
    }

    @Test
    void depositValidation_ReceiverError() {
        when(cardService.findByCardIdentifier(RECEIVER_LAST4)).thenReturn(null);

        var errors = assertThrows(MultipleValidationException.class,
                () -> validationService.validateOperation(OperationTypeEnum.DEPOSIT, depositDto));
        assertTrue(errors.getErrors().containsKey("receiver"));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN)
    void withdrawalValidation_Success() {
        when(clientService.findByLogin(CLIENT_LOGIN)).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId(), "sender")).thenReturn(senderCard);

        assertDoesNotThrow(() -> validationService.validateOperation(OperationTypeEnum.WITHDRAWAL, withdrawalDto));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN)
    void withdrawalValidation_Forbidden() {
        senderCard.setClient(new Client());
        when(clientService.findByLogin(CLIENT_LOGIN)).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId(), "sender")).thenReturn(senderCard);

        assertThrows(AccessDeniedException.class,
                () -> validationService.validateOperation(OperationTypeEnum.WITHDRAWAL, withdrawalDto));
    }

    @Test
    @WithMockUser(username = CLIENT_LOGIN)
    void withdrawalValidation_AmountError() {
        withdrawalDto.setAmount(BigDecimal.ZERO);
        when(clientService.findByLogin(CLIENT_LOGIN)).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId(), "sender")).thenReturn(senderCard);

        var errors = assertThrows(MultipleValidationException.class,
                () -> validationService.validateOperation(OperationTypeEnum.WITHDRAWAL, withdrawalDto));
        assertTrue(errors.getErrors().containsKey("amount"));
    }
}

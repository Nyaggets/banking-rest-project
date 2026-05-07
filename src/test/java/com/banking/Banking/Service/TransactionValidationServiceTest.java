package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.validation.MultipleValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(properties = {"TINK_KEYSET_BASE64=TINK_KEYSET_BASE64=ewogICAgInByaW1hcnlLZXlJZCI6IDIwMDczMzQ5MjAsCiAgICAia2V5IjogW3sKICAgICAgICAia2V5RGF0YSI6IHsKICAgICAgICAgICAgInR5cGVVcmwiOiAidHlwZS5nb29nbGVhcGlzLmNvbS9nb29nbGUuY3J5cHRvLnRpbmsuQWVzRWF4S2V5IiwKICAgICAgICAgICAgImtleU1hdGVyaWFsVHlwZSI6ICJTWU1NRVRSSUMiLAogICAgICAgICAgICAidmFsdWUiOiAiRWdJSUVCb2d1VDEwWU9Iek5hbTdETkRzbUJjMGdaejRNZlFHTVBNVERKM3RJbmlpUkV3PSIKICAgICAgICB9LAogICAgICAgICJvdXRwdXRQcmVmaXhUeXBlIjogIlRJTksiLAogICAgICAgICJrZXlJZCI6IDIwMDczMzQ5MjAsCiAgICAgICAgInN0YXR1cyI6ICJFTkFCTEVEIgogICAgfV0KfQ=="})
public class TransactionValidationServiceTest {
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
    @WithMockUser(username = "client")
    void transferValidation_Success() {
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId())).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.validateOperation(OperationTypes.TRANSFER, transferDto));
    }
    @Test
    @WithMockUser(username = "client")
    void transferValidation_Unauthorized() {
        senderCard.setClient(new Client());
        assertThrows(BadCredentialsException.class, () -> validationService.validateOperation(OperationTypes.TRANSFER, transferDto));
    }
    @Test
    @WithMockUser(username = "client")
    void transferValidation_ReceiverAmountErrors() {
        transferDto.setAmount(BigDecimal.ZERO);
        when(cardService.findByIdOrThrow(1L)).thenReturn(senderCard);
        when(cardService.findByCardIdentifier(anyString())).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var errors = assertThrows(MultipleValidationException.class, () -> validationService.validateOperation(OperationTypes.TRANSFER, transferDto));
        assertTrue(errors.getErrors().containsKey("receiver"));
        assertTrue(errors.getErrors().containsKey("amount"));
    }

    @Test
    void depositValidation_Success() {
        when(cardService.findByCardIdentifier(anyString())).thenReturn(receiverCard);

        assertDoesNotThrow(() -> validationService.validateOperation(OperationTypes.DEPOSIT, depositDto));
    }
    @Test
    void depositValidation_ReceiverError() {
        depositDto.setAmount(BigDecimal.ZERO);

        var errors = assertThrows(MultipleValidationException.class, () -> validationService.validateOperation(OperationTypes.DEPOSIT, depositDto));
        assertTrue(errors.getErrors().containsKey("receiver"));
    }

    @Test
    @WithMockUser(username = "client")
    void withdrawalValidation_Success() {
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);
        when(cardService.findByIdOrThrow(senderCard.getId())).thenReturn(senderCard);

        assertDoesNotThrow(() -> validationService.validateOperation(OperationTypes.WITHDRAWAL, withdrawalDto));
    }
    @Test
    @WithMockUser(username = "client")
    void withdrawalValidation_Forbidden() {
        senderCard.setClient(new Client());
        when(cardService.findByIdOrThrow(1L)).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        assertThrows(AccessDeniedException.class, () -> validationService.validateOperation(OperationTypes.WITHDRAWAL, withdrawalDto));
    }
    @Test
    @WithMockUser(username = "client")
    void withdrawalValidation_AmountError() {
        withdrawalDto.setAmount(BigDecimal.ZERO);
        when(cardService.findByIdOrThrow(1L)).thenReturn(senderCard);
        when(clientService.findByUsername(anyString())).thenReturn(senderClient);

        var errors = assertThrows(MultipleValidationException.class, () -> validationService.validateOperation(OperationTypes.WITHDRAWAL, withdrawalDto));
        assertTrue(errors.getErrors().containsKey("amount"));
    }
}

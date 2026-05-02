package com.banking.Banking.Dto;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.TransactionRepository;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.Service.TransactionService;
import com.banking.Banking.validation.DepositGroup;
import com.banking.Banking.validation.TransferGroup;
import com.banking.Banking.validation.WithdrawalGroup;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class TransactionDtoRequestTest {
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
                .cardNumber("1111111111111111111111")
                .build();

        receiverCard = Card.builder()
                .id(2L)
                .client(new Client())
                .balance(new BigDecimal("500"))
                .cardNumber("22222222222222222222")
                .build();

        transferDto = TransactionDtoRequest.builder()
                .senderCardId(1L)
                .receiverIdentifier("22222222222222222222")
                .amount(new BigDecimal("100"))
                .build();

        depositDto = TransactionDtoRequest.builder()
                .source("source")
                .receiverIdentifier("22222222222222222222")
                .amount(new BigDecimal("100"))
                .build();

        withdrawalDto = TransactionDtoRequest.builder()
                .senderCardId(1L)
                .merchant("merchant")
                .amount(new BigDecimal("100"))
                .build();
    }
    @Test
    void transferValidation_Success() {
        var transferResult = validator.validate(transferDto, TransferGroup.class, Default.class);
        System.out.println(transferResult);
        assertThat(transferResult).isEmpty();
    }
    @Test
    void transferValidation_InvalidData() {
        transferDto.setSenderCardId(null);
        transferDto.setReceiverIdentifier(null);
        transferDto.setAmount(new BigDecimal(4).negate());

        var transferResult = validator.validate(transferDto, TransferGroup.class, Default.class);

        assertThat(transferResult.size()).isEqualTo(3);
    }
    @Test
    void transferValidation_InvalidFiled() {
        transferDto.setMerchant(""); //поле, которое должно быть null в transfer операции
        var transferResult = validator.validate(transferDto, TransferGroup.class, Default.class);
        System.out.println(transferResult);

        assertThat(transferResult.size()).isEqualTo(1);
    }


    @Test
    void depositValidation_Success() {
        var depositResult = validator.validate(depositDto, DepositGroup.class, Default.class);
        assertThat(depositResult).isEmpty();
    }
    @Test
    void depositValidation_InvalidData() {
        depositDto.setSource(null);
        depositDto.setReceiverIdentifier(null);
        depositDto.setAmount(new BigDecimal(4).negate());

        var depositResult = validator.validate(depositDto, DepositGroup.class, Default.class);

        assertThat(depositResult.size()).isEqualTo(3);
    }
    @Test
    void depositValidation_InvalidFiled() {
        depositDto.setMerchant(""); //поле, которое должно быть null в deposit операции
        var depositResult = validator.validate(depositDto, DepositGroup.class, Default.class);

        assertThat(depositResult.size()).isEqualTo(1);
    }

    @Test
    void withdrawalValidation_Success() {
        var withdrawalResult = validator.validate(withdrawalDto, WithdrawalGroup.class, Default.class);
        assertThat(withdrawalResult).isEmpty();
    }
    @Test
    void withdrawalValidation_InvalidData() {
        withdrawalDto.setMerchant(null);
        withdrawalDto.setSenderCardId(null);
        withdrawalDto.setAmount(new BigDecimal(4).negate());

        var withdrawalResult = validator.validate(withdrawalDto, WithdrawalGroup.class, Default.class);

        assertThat(withdrawalResult.size()).isEqualTo(3);
    }
    @Test
    void withdrawalValidation_InvalidFiled() {
        withdrawalDto.setReceiverIdentifier(""); //поле, которое должно быть null в withdrawal операции
        var withdrawalResult = validator.validate(withdrawalDto, WithdrawalGroup.class, Default.class);

        assertThat(withdrawalResult.size()).isEqualTo(1);
    }
    @Test
    void depositValidation_PatternSuccess() {
        TransactionDtoRequest depositCard = depositDto;
        TransactionDtoRequest depositPhone = depositDto;
        depositPhone.setReceiverIdentifier("89022213880");

        var depositCardResult = validator.validate(depositCard, DepositGroup.class, Default.class);
        var depositPhoneResult = validator.validate(depositPhone, DepositGroup.class, Default.class);

        assertThat(depositCardResult.size()).isEqualTo(0);
        assertThat(depositPhoneResult.size()).isEqualTo(0);
    }
    @Test
    void depositValidation_InvalidPattern() {
        TransactionDtoRequest depositCard = depositDto;
        depositCard.setReceiverIdentifier("invalid");
        TransactionDtoRequest depositPhone = depositDto;
        depositPhone.setReceiverIdentifier("invalid");

        var depositCardResult = validator.validate(depositCard, DepositGroup.class, Default.class);
        var depositPhoneResult = validator.validate(depositPhone, DepositGroup.class, Default.class);

        assertThat(depositCardResult.size()).isEqualTo(1);
        assertThat(depositPhoneResult.size()).isEqualTo(1);
    }
}

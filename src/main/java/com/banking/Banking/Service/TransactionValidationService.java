package com.banking.Banking.Service;

import com.banking.Banking.Dto.DepositDtoRequest;
import com.banking.Banking.Dto.TransferDtoRequest;
import com.banking.Banking.Dto.WithdrawalDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.validation.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

/**
 * Сервис валидации входных данных транзакций
 */
@Service
public class TransactionValidationService {
    @Autowired
    private CardService cardService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private VerifyIdentityService identityService;

    private void amountValidation(BigDecimal senderBalance, BigDecimal amount, HashMap<String, String> errors) {
        if (senderBalance.compareTo(amount) < 0)
            errors.put("amount", "На карте отправителя недостаточно средств");
        if (amount.compareTo(new BigDecimal("1000000")) > 0 || amount.compareTo(BigDecimal.TEN) < 0)
            errors.put("amount", "Разовый перевод должен быть от 10₽ до 1 000 000₽ включительно");
    }

    private void clientSenderValidation(Long currentClientId, Card senderCard, HashMap<String, String> errors) {
        clientService.findByIdOrThrow(currentClientId);
        if (senderCard == null || cardService.findById(senderCard.getId()) == null) {
            errors.put("sender", "Карта отправителя не найдена");
            return;
        }
        if (!Objects.equals(senderCard.getClient().getId(), currentClientId))
            throw new AccessDeniedException("Доступ к карте запрещён");
    }

    private boolean receiverValidation(String receiverIdentifier, HashMap<String, String> errors) {
        Card receiverCard = cardService.findByCardIdentifier(receiverIdentifier);
        if (receiverCard == null) {
            errors.put("receiver", "Получатель не найден");
            return false;
        }
        return true;
    }

    public void transferValidation(Long currentClientId, TransferDtoRequest transactionDto) {
        HashMap<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getClientCardId(), "sender");

        clientSenderValidation(currentClientId, senderCard, errors);
        amountValidation(senderCard.getBalance(), transactionDto.getAmount(), errors);
        if (!receiverValidation(transactionDto.getCounterpartyCardIdentifier(), errors))
            throw new CustomException("VALIDATION EXCEPTION", errors);
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getCounterpartyCardIdentifier());
        if (senderCard.getId().equals(receiverCard.getId()))
            errors.put("receiver", "Карта получателя совпадает с картой отправителя");
        if (!errors.isEmpty())
            throw new CustomException("VALIDATION EXCEPTION", errors);
    }

    public void withdrawalValidation(Long currentClientId, WithdrawalDtoRequest transactionDto) {
        HashMap<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getClientCardId(), "sender");

        clientSenderValidation(currentClientId, senderCard, errors);
        amountValidation(senderCard.getBalance(), transactionDto.getAmount(), errors);
        if (!errors.isEmpty())
            throw new CustomException("VALIDATION EXCEPTION", errors);
    }

    public void depositValidation(DepositDtoRequest transactionDto) {
        HashMap<String, String> errors = new HashMap<>();

        receiverValidation(transactionDto.getCounterpartyIdentifier(), errors);
        if (!errors.isEmpty())
            throw new CustomException("VALIDATION EXCEPTION", errors);
    }
}

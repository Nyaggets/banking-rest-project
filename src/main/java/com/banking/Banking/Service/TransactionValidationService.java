package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.validation.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Objects;

@Service
public class TransactionValidationService {
    @Autowired
    private CardService cardService;
    @Autowired
    private ClientService clientService;
    @Autowired
    private VerifyIdentityService identityService;

    private void amountValidation(BigDecimal senderBalance, BigDecimal amount, HashMap<String, String> errors) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            errors.put("amount", "Некорректная сумма перевода");
        if (senderBalance.compareTo(amount) < 0)
            errors.put("amount", "На карте отправителя недостаточно средств");
        if (amount.compareTo(new BigDecimal("1000000")) > 0 || amount.compareTo(BigDecimal.TEN) < 0)
            errors.put("amount", "Разовый перевод должен быть от 10₽ до 1 000 000₽ включительно");
    }

    private boolean clientSenderValidation(Card senderCard, HashMap<String, String> errors) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client client = clientService.findByLoginOrThrow(auth.getName());
        if (senderCard == null) {
            errors.put("sender", "Карта отправителя не найдена");
            return false;
        }
        if (!Objects.equals(senderCard.getClient().getId(), client.getId()))
            throw new AccessDeniedException("Доступ к карте запрещён");
        return true;
    }

    private boolean receiverValidation(String receiverIdentifier, HashMap<String, String> errors) {
        Card receiverCard = cardService.findByCardIdentifier(receiverIdentifier);
        if (receiverCard == null) {
            errors.put("receiver", "Получатель не найден");
            return false;
        }
        return true;
    }

    public HashMap<String, String> transferValidation(TransactionDtoRequest transactionDto) {
        HashMap<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getClientCardId(), "sender");

        clientSenderValidation(senderCard, errors);
        amountValidation(senderCard.getBalance(), transactionDto.getAmount(), errors);
        if (!receiverValidation(transactionDto.getReceiverIdentifier(), errors))
            return errors;
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        if (senderCard.getId().equals(receiverCard.getId()))
            errors.put("receiver", "Карта получателя совпадает с картой отправителя");
        return errors;
    }
    public HashMap<String, String> withdrawalValidation(TransactionDtoRequest transactionDto) {
        HashMap<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getClientCardId(), "sender");

        clientSenderValidation(senderCard, errors);
        amountValidation(senderCard.getBalance(), transactionDto.getAmount(), errors);
        return errors;
    }

    public HashMap<String, String> depositValidation(TransactionDtoRequest transactionDto) {
        HashMap<String, String> errors = new HashMap<>();

        receiverValidation(transactionDto.getReceiverIdentifier(), errors);
        return errors;
    }

    public void validateOperation(OperationTypes type, TransactionDtoRequest dtoRequest) {
        var transactionError = switch (type) {
            case OperationTypes.TRANSFER_OUT, OperationTypes.TRANSFER_IN -> transferValidation(dtoRequest);
            case OperationTypes.WITHDRAWAL -> withdrawalValidation(dtoRequest);
            case OperationTypes.DEPOSIT -> depositValidation(dtoRequest);
        };
        if (!transactionError.isEmpty())
            throw new CustomException("VALIDATION EXCEPTION", transactionError);
    }
}

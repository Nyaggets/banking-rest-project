package com.banking.Banking.Service;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class TransactionService {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private CardService cardService;
    @Autowired
    private ClientService clientService;

    private void isAmountValid(BigDecimal senderBalance, BigDecimal amount, Map<String, String> errors) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            errors.put("amount", "Некорректная сумма перевода");
        if (senderBalance.compareTo(amount) < 0)
            errors.put("amount", "На карте отправителя недостаточно средств");
    }

    private boolean clientSenderValidation(Card senderCard, Map<String, String> errors) {
        boolean isValid = true;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client client = clientService.findByUsername(auth.getName());
        if (client == null) {
            errors.put("operation error", "Пользователь не найден");
            return false;
        }
        if (senderCard == null) {
            errors.put("sender", "Карта отправителя не найдена");
            return false;
        }
        if (!Objects.equals(senderCard.getClient().getId(), client.getId())) {
            errors.put("sender", "Карта не принадлежит текущему пользователю");
            isValid = false;
        }
        return isValid;
    }

    private boolean receiverValidation(Card receiverCard, Map<String, String> errors) {
        if (receiverCard == null) {
            errors.put("receiver", "Получатель не найден");
            return false;
        }
        return true;
    }


    public Map<String, String> transferValidation(TransactionDtoRequest transactionDto) {
        Map<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findById(transactionDto.getSenderCardId());
        Card receiverCard = cardService.findByCardNumber(transactionDto.getReceiverCardNumber());

        if (!clientSenderValidation(senderCard, errors))
            return errors;
        isAmountValid(senderCard.getBalance(), transactionDto.getAmount(), errors);
        if (!receiverValidation(receiverCard, errors))
            return errors;
        if (senderCard.getId().equals(receiverCard.getId()))
            errors.put("receiver", "Карта получателя совпадает с картой отправителя");
        return errors;
    }

    public Transaction createTransfer(TransactionDtoRequest transactionDto) {
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getSenderCardId());
        Card receiverCard = cardService.findByCardNumberOrThrow(transactionDto.getReceiverCardNumber());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.TRANSFER)
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .amount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();

        BigDecimal senderBalance = transaction.getSenderCard().getBalance();
        transaction.getSenderCard().setBalance(senderBalance.subtract(transaction.getAmount()));
        receiverCard.setBalance(receiverCard.getBalance().add(transaction.getAmount()));
        return repository.save(transaction);
    }

    public Map<String, String> withdrawalValidation(TransactionDtoRequest transactionDto) {
        Map<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findById(transactionDto.getSenderCardId());

        clientSenderValidation(senderCard, errors);
        isAmountValid(senderCard.getBalance(), transactionDto.getAmount(), errors);
        return errors;
    }

    public Transaction createWithdrawal(TransactionDtoRequest transactionDto) {
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getSenderCardId());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.WITHDRAWAL)
                .senderCard(senderCard)
                .merchant(transactionDto.getMerchant())
                .amount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal cardBalance = senderCard.getBalance();
        if (transaction.getAmount().compareTo(cardBalance) == 1 ||
                transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            return null;
        }
        senderCard.setBalance(cardBalance.subtract(transaction.getAmount()));
        return repository.save(transaction);
    }

    public Map<String, String> depositValidation(TransactionDtoRequest transactionDto) {
        Map<String, String> errors = new HashMap<>();
        Card receiverCard = cardService.findByCardNumber(transactionDto.getReceiverCardNumber());

        receiverValidation(receiverCard, errors);
        if (transactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            errors.put("amount", "Некорректная сумма перевода");
        return errors;
    }

    public Transaction createDeposit(TransactionDtoRequest transactionDto) {
        Card receiverCard = cardService.findByCardNumberOrThrow(transactionDto.getReceiverCardNumber());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.DEPOSIT)
                .source(transactionDto.getSource())
                .receiverCard(receiverCard)
                .amount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = receiverCard.getBalance();
        receiverCard.setBalance(senderBalance.add(transactionDto.getAmount()));
        return repository.save(transaction);
    }

    public List<Transaction> findByCardId(Long cardId) {
        return repository.findByCardId(cardId);
    }

    public List<Transaction> findByClientId(Long clientId) {
        List<Card> cards = cardService.findByClientId(clientId);
        return cards.stream()
                    .flatMap(card -> repository.findByCardId(card.getId()).stream())
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .distinct()
                    .toList();
    }
}

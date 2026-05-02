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

    public BigDecimal calculateCommission(BigDecimal amount) {
        return amount.compareTo(new BigDecimal("100000")) < 0
                ? BigDecimal.ZERO
                : amount.multiply(new BigDecimal("0.05"));
    }

    private void isAmountValid(BigDecimal senderBalance, BigDecimal amount, Map<String, String> errors) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            errors.put("amount", "Некорректная сумма перевода");
        if (senderBalance.compareTo(amount) < 0)
            errors.put("amount", "На карте отправителя недостаточно средств");
        if (amount.compareTo(new BigDecimal("1000000")) > 0 || amount.compareTo(BigDecimal.TEN) < 0)
            errors.put("amount", "Разовый перевод должен быть от 10₽ до 1 000 000₽ включительно");
    }

    private boolean clientSenderValidation(Card senderCard, Map<String, String> errors) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Client client = clientService.findByUsername(auth.getName());
        if (client == null) {
            errors.put("unauthorized", "Пользователь не найден");
            return false;
        }
        if (senderCard == null) {
            errors.put("sender", "Карта отправителя не найдена");
            return false;
        }
        if (!Objects.equals(senderCard.getClient().getId(), client.getId())) {
            errors.put("forbidden", "Карта не принадлежит текущему пользователю");
            return false;
        }
        return true;
    }

    private boolean receiverValidation(String receiverIdentifier, Map<String, String> errors) {
        Card receiverCard = cardService.findByCardIdentifier(receiverIdentifier);
        if (receiverCard == null) {
            errors.put("receiver", "Получатель не найден");
            return false;
        }
        return true;
    }

    public Map<String, String> transferValidation(TransactionDtoRequest transactionDto) {
        Map<String, String> errors = new HashMap<>();
        Card senderCard = cardService.findById(transactionDto.getSenderCardId());

        clientSenderValidation(senderCard, errors);
        isAmountValid(senderCard.getBalance(), transactionDto.getAmount(), errors);
        receiverValidation(transactionDto.getReceiverIdentifier(), errors);
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        if (senderCard.getId().equals(receiverCard.getId()))
            errors.put("receiver", "Карта получателя совпадает с картой отправителя");
        return errors;
    }

    public Transaction createTransfer(TransactionDtoRequest transactionDto) {
        Card senderCard = cardService.findByIdOrThrow(transactionDto.getSenderCardId());
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        BigDecimal commission = calculateCommission(transactionDto.getAmount());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.TRANSFER)
                .senderCard(senderCard)
                .receiverCard(receiverCard)
                .amount(transactionDto.getAmount())
                .commission(commission)
                .totalAmount(transactionDto.getAmount().add(commission))
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = transaction.getSenderCard().getBalance();
        transaction.getSenderCard().setBalance(senderBalance.subtract(transaction.getTotalAmount()));
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
        BigDecimal commission = calculateCommission(transactionDto.getAmount());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.WITHDRAWAL)
                .senderCard(senderCard)
                .merchant(transactionDto.getMerchant())
                .amount(transactionDto.getAmount())
                .commission(commission)
                .totalAmount(transactionDto.getAmount().add(commission))
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal cardBalance = senderCard.getBalance();
        if (transaction.getAmount().compareTo(cardBalance) > 0 ||
                transaction.getAmount().compareTo(BigDecimal.ZERO) <= 0){
            return null;
        }
        senderCard.setBalance(cardBalance.subtract(transaction.getTotalAmount()));
        return repository.save(transaction);
    }

    public Map<String, String> depositValidation(TransactionDtoRequest transactionDto) {
        Map<String, String> errors = new HashMap<>();

        receiverValidation(transactionDto.getReceiverIdentifier(), errors);
        if (transactionDto.getAmount().compareTo(BigDecimal.ZERO) <= 0)
            errors.put("amount", "Некорректная сумма перевода");
        return errors;
    }

    public Transaction createDeposit(TransactionDtoRequest transactionDto) {
        Card receiverCard = cardService.findByCardIdentifier(transactionDto.getReceiverIdentifier());
        Transaction transaction = Transaction.builder()
                .type(OperationTypes.DEPOSIT)
                .source(transactionDto.getSource())
                .receiverCard(receiverCard)
                .amount(transactionDto.getAmount())
                .totalAmount(transactionDto.getAmount())
                .description(transactionDto.getDescription())
                .timestamp(LocalDateTime.now())
                .build();
        BigDecimal senderBalance = receiverCard.getBalance();
        receiverCard.setBalance(senderBalance.add(transaction.getTotalAmount()));
        return repository.save(transaction);
    }

    public List<Transaction> findByCardId(Long cardId) {
        return repository.findByCardId(cardId);
    }

    public List<Transaction> findByClientId(Long clientId) {
        List<Card> cards = cardService.findByClientId(clientId);
        return cards.stream()
                    .flatMap(card -> repository.findByCardId(card.getId()).stream())
                    .sorted(Comparator.comparing(Transaction::getTimestamp).reversed())
                    .distinct()
                    .toList();
    }
}

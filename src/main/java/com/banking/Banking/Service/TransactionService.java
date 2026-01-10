package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class TransactionService {
    @Autowired
    private TransactionRepository repository;
    @Autowired
    private CardService cardService;

    public Transaction createTransfer(Transaction transaction, Long senderCardId){
        Card senderCard = cardService.findById(senderCardId);
        if (senderCard == null || transaction.getReceiverCard() == null){
            return null;
        }
        transaction.setSenderCard(senderCard);
        if (transaction.getReceiverCard() == null ||
                transaction.getReceiverCard().equals(transaction.getSenderCard()) ||
                transaction.getAmount().compareTo(new BigDecimal("0")) <= 0){
            return null;
        }
        transaction.setType("Перевод");
        transaction.setTimestamp(LocalDateTime.now());
        return repository.save(transaction);
    }

    public Transaction createWriteOff(Transaction transaction, Long senderCardId){
        Card senderCard = cardService.findById(senderCardId);
        if (senderCard == null){
            return null;
        }
        BigDecimal cardBalance = senderCard.getBalance();
        if (transaction.getAmount().compareTo(cardBalance) == 1 ||
                transaction.getAmount().compareTo(new BigDecimal("0")) <= 0){
            return null;
        }
        transaction.setType("Оплата товаров и услуг");
        senderCard.setBalance(cardBalance.subtract(transaction.getAmount()));
        transaction.setTimestamp(LocalDateTime.now());
        return repository.save(transaction);
    }

    public Transaction createReplenish(Transaction transaction, Long receiverCardId){
        Card receiverCard = cardService.findById(receiverCardId);
        if (receiverCard == null){
            return null;
        }
        BigDecimal cardBalance = receiverCard.getBalance();
        if (transaction.getAmount().compareTo(new BigDecimal("0")) <= 0){
            return null;
        }
        transaction.setType("Зачисление");
        receiverCard.setBalance(cardBalance.add(transaction.getAmount()));
        transaction.setReceiverCard(receiverCard);
        transaction.setTimestamp(LocalDateTime.now());
        return repository.save(transaction);
    }

    public List<Transaction> findByCardId(Long cardId){
        return repository.findByCardId(cardId);
    }

    public List<Transaction> findByClientId(Long clientId){
        List<Card> cards = cardService.findByClientId(clientId);
        return cards.stream()
                    .flatMap(card -> repository.findByCardId(card.getId()).stream())
                    .sorted(Comparator.comparing(Transaction::getTimestamp))
                    .distinct()
                    .toList();
    }
}

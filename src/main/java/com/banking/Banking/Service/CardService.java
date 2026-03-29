package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.validation.CardNotFoundException;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Service
@Transactional
public class CardService {
    @Autowired
    private CardRepository repository;
    @Autowired
    private ClientService clientService;

    public String generateCardNumber(){
        String numberBase = "123456";
        Random random = new Random();
        for (int i = 0; i < 14; i++){
            numberBase += random.nextInt(10);
        }
        return numberBase;
    }

    public Card createCard(Long clientId){
        Client client = clientService.findById(clientId);
        if (client == null){
            throw new EntityNotFoundException("Пользователь не найден");
        }

        String cardNumber = generateCardNumber();
        while (repository.findByCardNumber(cardNumber).isPresent()){
            cardNumber = generateCardNumber();
        }
        Card card = Card.builder()
                        .client(client)
                        .clientName(client.getName())
                        .cardNumber(cardNumber)
                        .balance(BigDecimal.ZERO)
                        .createdDate(LocalDate.now())
                        .build();
        return repository.save(card);
    }

    public Card findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Card findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
    }

    public Card findByCardNumber(String number) {
        return repository.findByCardNumber(number).orElse(null);
    }

    public Card findByCardNumberOrThrow(String number) {
        return repository.findByCardNumber(number)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена по номеру"));
    }

    public List<Card> findByClientId(Long id){
        if (clientService.findById(id) == null){
            return null;
        }
        return repository.findAllByClientId(id).stream()
                .sorted(Comparator.comparing(Card::getBalance).reversed())
                .toList();
    }

    public boolean deleteCard(Long id){
        if (repository.findById(id).orElse(null) == null){
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}

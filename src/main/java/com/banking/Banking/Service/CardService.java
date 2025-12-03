package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
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
        if (clientService.findById(clientId) == null){
            return null;
        }

        String cardNumber = generateCardNumber();
        while (repository.findByCardNumber(cardNumber).isPresent()){
            cardNumber = generateCardNumber();
        }

        Card card = new Card();
        card.setClient(clientService.findById(clientId));
        card.setCardNumber(cardNumber);
        card.setBalance(new BigDecimal(0));
        card.setCreatedDate(LocalDate.now());
        return repository.save(card);
    }

    public Card findById(Long id){
        return repository.findById(id).orElse(null);
    }

    public Card findByCardNumber(String cardNumber){
        return repository.findByCardNumber(cardNumber).orElse(null);
    }

    public List<Card> findAllByClientId(Long id){
        if (clientService.findById(id) == null){
            return null;
        }
        return repository.findAllByClientId(id);
    }

    public boolean deleteCard(Long id){
        if (repository.findById(id).orElse(null) == null){
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}

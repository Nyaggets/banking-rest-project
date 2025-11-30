package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class CardService {
    @Autowired
    private CardRepository repository;

    public String generateCardNumber(){
        String cardNumber = "123456";
        Random random = new Random();
        for (int i = 0; i < 14; i++){
            cardNumber += random.nextInt(10);
        }
        return cardNumber;
    }

    public Card createCard(Card card){
        String cardNumber = generateCardNumber();
        while (repository.findByCardNumber(cardNumber).isPresent()){
            cardNumber = generateCardNumber();
        }

        card.setCardNumber(cardNumber);
        repository.save(card);
        return card;
    }

    public Card findByCardNumber(String cardNumber){
        return repository.findByCardNumber(cardNumber).orElse(null);
    }

    public boolean deleteCard(Long id){
        if (repository.findById(id).orElse(null) == null){
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}

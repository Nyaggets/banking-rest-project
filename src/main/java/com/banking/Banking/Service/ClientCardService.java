package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientCardService {
    @Autowired
    private CardService cardService;
    @Autowired
    private ClientService clientService;

    public Card createCardForUser(Card card, Long clientId){
        if (clientService.findById(clientId) == null){
            return null;
        }
        card.setClient(clientService.findById(clientId));
        return cardService.createCard(card);
    }
}

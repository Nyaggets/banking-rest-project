package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("clients/{clientId}/cards")
public class CardController {
    @Autowired
    private CardService cardService;
    @Autowired
    private CardMapper mapper;

    @GetMapping
    public ResponseEntity<List<CardDtoResponse>> findAllByClientId(@PathVariable Long clientId){
        List<Card> cards = cardService.findAllByClientId(clientId);
        if (cards == null){
            return ResponseEntity.notFound().build();
        }
        List<CardDtoResponse> cardsDto = mapper.toListDtoResponse(cards);
        return ResponseEntity.ok(cardsDto);
    }

    @GetMapping("search-id/{id}")
    public ResponseEntity<CardDtoResponse> findById(@PathVariable Long id){
        Card card = cardService.findById(id);
        if (card == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(card));
    }

    @GetMapping("search-number/{number}")
    public ResponseEntity<CardDtoResponse> findByCardNumber(@PathVariable String number){
        Card card = cardService.findByCardNumber(number);
        if (card == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(card));
    }

    @PostMapping("/create")
    public ResponseEntity<CardDtoResponse> createCardForClient(@PathVariable Long clientId){
        Card card = cardService.createCard(clientId);
        if (card == null){
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(mapper.toDtoResponse(card), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        if (!cardService.deleteCard(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}

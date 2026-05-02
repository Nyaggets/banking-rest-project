package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
        List<Card> cards = cardService.findByClientId(clientId);
        if (cards == null){
            return ResponseEntity.notFound().build();
        }
        List<CardDtoResponse> cardsDto = mapper.toListDto(cards);
        return ResponseEntity.ok(cardsDto);
    }

    @GetMapping("/card")
    @ResponseBody
    public ResponseEntity<CardDtoResponse> findById(@PathVariable Long clientId,
                                                    @RequestParam String id){
        Card card = cardService.findById(Long.valueOf(id));
        if (card == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDto(card));
    }

    @GetMapping("search")
    public ResponseEntity<CardDtoResponse> findByCardNumber(@RequestParam String number){
        Card card = cardService.findByCardNumberHash(number);
        if (card == null){
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(mapper.toDto(card));
    }

    @PostMapping
    public ResponseEntity<?> createCardForClient(@PathVariable Long clientId){
        try {
            Card card = cardService.createCard(clientId);
            return new ResponseEntity<>(mapper.toDto(card), HttpStatus.CREATED);
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }

    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        if (!cardService.deleteCard(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }


}

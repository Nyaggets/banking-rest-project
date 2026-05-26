package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Dto.CardStatsDto;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.TransactionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("clients/{clientId}/cards")
public class CardController {
    @Autowired
    private CardService cardService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CardMapper mapper;

    @GetMapping
    @ResponseBody
    public ResponseEntity<CardDtoResponse> findById(@PathVariable Long clientId, @RequestParam String id) {
        Card card = cardService.findByIdOrThrow(Long.valueOf(id));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(mapper.toDto(card));
    }

    @GetMapping("{id}/stats")
    @ResponseBody
    public ResponseEntity<CardStatsDto> cardStats(@PathVariable Long clientId, @PathVariable Long id) throws AccessDeniedException {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(transactionService.getMonthlyStats(id, clientId));
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<?> deleteById(@PathVariable Long id){
        if (!cardService.deleteCard(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}

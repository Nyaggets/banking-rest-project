package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Dto.CardStatsDto;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.SessionUser;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.TransactionService;
import com.banking.Banking.validation.CustomException;
import com.banking.Banking.validation.CustomNotFoundException;
import com.banking.Banking.validation.RequestLimitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Random;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/cards")
public class CardController {
    @Autowired
    private CardService cardService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private CardMapper mapper;

    @GetMapping
    public ResponseEntity<List<CardDtoResponse>> findByClientId(Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(mapper.toListDto(cardService.findByClientId(client.getId())));
    }

    @GetMapping("/{cardId}")
    public ResponseEntity<CardDtoResponse> findByCardId(Authentication auth, @PathVariable Long cardId) {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(mapper.toDto(cardService.saveFindById(client.getId(), cardId)));
    }

    @GetMapping("{cardId}/stats")
    public ResponseEntity<CardStatsDto> cardStats(Authentication auth, @PathVariable Long cardId) throws AccessDeniedException {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(transactionService.getMonthlyStats(cardId, client.getId()));
    }

    @PostMapping("{cardId}/card-details")
    public ResponseEntity<?> revealCardDetails(Authentication auth, @PathVariable String cardId,
                                               @RequestBody Map<String, String> requestBody) throws  AccessDeniedException {
        SessionUser client = (SessionUser) auth.getPrincipal();
        Map<String, String> details = cardService.revealCardDetails(client.getId(),
                requestBody.get("password"), Long.valueOf(cardId));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(details);
    }

    @GetMapping("/owner")
    @ResponseBody
    public ResponseEntity<?> getRecipientInfo(@RequestParam String identifier) {
        String owner = cardService.getOwner(identifier);
        return ResponseEntity.ok(Map.of("fullName", owner));
    }
}

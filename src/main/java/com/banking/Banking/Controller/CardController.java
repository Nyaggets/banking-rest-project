package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoResponse;
import com.banking.Banking.Dto.CardStatsDto;
import com.banking.Banking.Entity.SessionUser;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /**
     * Получение списка карт текущего пользователя
     */
    @GetMapping
    public ResponseEntity<List<CardDtoResponse>> findByClientId(Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(mapper.toListDto(cardService.findByClientId(client.getId())));
    }

    /**
     * Получение данных конкретной карты текущего пользователя
     */
    @GetMapping("/{cardId}")
    public ResponseEntity<CardDtoResponse> findByCardId(Authentication auth, @PathVariable Long cardId) {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(mapper.toDto(cardService.safeFindById(client.getId(), cardId)));
    }

    /**
     * Получение статистики переводов для конкретной карты текущего пользователя
     */
    @GetMapping("{cardId}/stats")
    public ResponseEntity<CardStatsDto> cardStats(Authentication auth, @PathVariable Long cardId) throws AccessDeniedException {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(transactionService.getMonthlyStats(cardId, client.getId()));
    }

    /**
     * Получение полных данных конкретной карты текущего пользователя
     */
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

    /**
     * Получение пользователя карты по её идентификатору
     */
    @GetMapping("/owner")
    @ResponseBody
    public ResponseEntity<?> getRecipientInfo(@RequestParam String identifier) {
        String owner = cardService.getOwner(identifier);
        return ResponseEntity.ok(Map.of("fullName", owner));
    }
}

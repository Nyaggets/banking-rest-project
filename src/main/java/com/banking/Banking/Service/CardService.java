package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import com.banking.Banking.validation.RequestLimitException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
public class CardService {
    private final CardRepository repository;
    private final ClientService clientService;
    private final EncodeService encodeService;
    private final VerifyIdentityService attemptsCount;

    public CardService(CardRepository repository, ClientService clientService, EncodeService encodeService, VerifyIdentityService attemptsCount) {
        this.repository = repository;
        this.clientService = clientService;
        this.encodeService = encodeService;
        this.attemptsCount = attemptsCount;
    }

    public String generateCardNumber(){
        StringBuilder numberBase = new StringBuilder("123456");
        Random random = new Random();
        for (int i = 0; i < 14; i++)
            numberBase.append(random.nextInt(10));
        return numberBase.toString();
    }

    public String generateCVV() {
        StringBuilder cvvString = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 3; i++){
            cvvString.append(random.nextInt(10));
        }
        return cvvString.toString();
    }

    public Card createCard(Long clientId){
        Client client = clientService.findByIdOrThrow(clientId);
        String cardNumber = generateCardNumber();
        while (repository.findByCardNumber(cardNumber).isPresent())
            cardNumber = generateCardNumber();

        String cvv = generateCVV();
        while (repository.findByCVV(cvv).isPresent())
            cvv = generateCVV();

        Card card = Card.builder()
                        .client(client)
                        .clientName(client.getName())
                        .cardNumber(encodeService.encodeString(cardNumber, clientId))
                        .cvv(encodeService.encodeString(cvv, clientId))
                        .balance(BigDecimal.ZERO)
                        .createdDate(LocalDate.now())
                        .expiredDate(LocalDate.now().plusYears(7))
                        .last4(cardNumber.substring(16))
                        .cardNumberHash(encodeService.generateSha256Hash(cardNumber))
                        .cvvHash(encodeService.generateSha256Hash(cvv))
                        .build();
        return repository.save(card);
    }

    public Card findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Card saveFindById(Long clientId, Long cardId) {
        belongsToClient(clientId, cardId);
        return findByIdOrThrow(cardId);
    }

    public Card findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Карта не найдена"));
    }

    public Card findByCardNumberHash(String number) {
        String numberHash = encodeService.generateSha256Hash(number);
        return repository.findByCardNumberHash(numberHash).orElse(null);
    }

    public Card findByLast4(String last4) {
        return repository.findByLast4(last4).orElse(null);
    }

    public Card findByCardNumberOrThrow(String number) {
        return repository.findByCardNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("Карта с данным номером не найдена"));
    }

    public boolean belongsToClient(Long clientId, Long cardId) {
        Card card = findByIdOrThrow(cardId);
        return card.getClient().getId().equals(clientId);
    }

    public Card findByCardIdentifier(String identifier) {
        Card card;
        if (identifier.matches("^(\\+7|8)\\d{10}$")) {
            Client client = clientService.findByPhone(identifier);
            card = findByClientId(client.getId()).getFirst();
        }
        else if (identifier.matches("^\\d{20}$"))
            card = findByCardNumberHash(identifier);
        else if (identifier.matches("^\\d{4}$"))
            card = findByLast4(identifier);
        else
            card = findById(Long.valueOf(identifier));
        return card;
    }

    public List<Card> findByClientId(Long clientId){
        clientService.findByIdOrThrow(clientId);
        return repository.findAllByClientId(clientId).stream()
                .sorted(Comparator.comparing(Card::getBalance).reversed())
                .toList();
    }

    public boolean deleteCard(Long id){
        if (repository.findById(id).orElse(null) == null)
            return false;

        repository.deleteById(id);
        return true;
    }

    public Map<String, String> revealCardDetails(Long clientId, String password, Long cardId) throws RequestLimitException, AccessDeniedException {
        findByIdOrThrow(cardId);
        attemptsCount.throwIfAttemptLimit(clientId, password, clientService.checkPassword(password, clientId));
        Card card = findByIdOrThrow(cardId);
        return new HashMap<>() {{
            put("cvv", encodeService.decodeString(card.getCvv(), clientId));
            put("cardNumber", encodeService.decodeString(card.getCardNumber(), clientId));
        }};
    }
}

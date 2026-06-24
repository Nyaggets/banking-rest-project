package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import com.banking.Banking.validation.CustomNotFoundException;
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
    private final VerifyIdentityService identityService;
    public CardService(CardRepository repository, ClientService clientService, EncodeService encodeService, VerifyIdentityService identityService) {
        this.repository = repository;
        this.clientService = clientService;
        this.encodeService = encodeService;
        this.identityService = identityService;
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

        Random random = new Random();
        StringBuilder accountNumber = new StringBuilder();
        for (int i = 0; i < 20; i++)
            accountNumber.append(random.nextInt(10));

        StringBuilder pinCode = new StringBuilder();
        for (int i = 0; i < 4; i++)
            pinCode.append(random.nextInt(10));

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
                        .accountNumber(accountNumber.toString())
                        .pinCode(encodeService.encodeString(pinCode.toString(), clientId))
                        .pinCodeHash(encodeService.generateSha256Hash(pinCode.toString()))
                        .build();
        return repository.save(card);
    }

    public Card findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Card safeFindById(Long clientId, Long cardId) {
        Card card = findByIdOrThrow(cardId, "card");
        if (Objects.equals(card.getClient().getId(), clientId))
            return card;
        throw new CustomNotFoundException("Карта не принадлежит пользователю", "client-card");
    }

    public Card findByIdOrThrow(Long id, String field) {
        return repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Карта не найдена", field));
    }

    public Card findByCardNumberHash(String number) {
        String numberHash = encodeService.generateSha256Hash(number);
        return repository.findByCardNumberHash(numberHash).orElse(null);
    }

    public Card findByLast4(String last4) {
        return repository.findByLast4(last4).orElse(null);
    }

    public void belongsToClientOrThrow(Long clientId, Card card) {
        if (!Objects.equals(card.getClient().getId(), clientId))
            throw new AccessDeniedException("Доступ к карте запрещен");
    }

    public Card findByCardIdentifier(String identifier) {
        Card card;
        if (identifier.matches("^\\d{20}$"))
            card = findByCardNumberHash(identifier);

        else if (identifier.matches("^\\d{4}$"))
            card = findByLast4(identifier);

        else if (identifier.matches("^(\\+?7|8)\\d{10}$")) {
            Client client = clientService.findByPhone(identifier);
            if (client == null)
                throw new CustomNotFoundException("Получатель не найден", "receiver");
            List<Card> clientCards = findByClientId(client.getId());
            if (clientCards == null || clientCards.isEmpty())
                throw new CustomNotFoundException("У получателя нет привязанных карт", "receiver");
            return clientCards.getFirst();
        }

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

    /**
     * Метод для получения полных данных карты с учетом количества попыток подтверждения личности
     */
    public Map<String, String> revealCardDetails(Long clientId, String password, Long cardId) throws AccessDeniedException {
        Card card = findByIdOrThrow(cardId, "sender");
        belongsToClientOrThrow(clientId, card);
        identityService.throwIfPasswordAttemptLimit(clientId, clientService.checkPassword(password, clientId));
        return new HashMap<>() {{
            put("cvv", encodeService.decodeString(card.getCvv(), clientId));
            put("cardNumber", encodeService.decodeString(card.getCardNumber(), clientId));
        }};
    }

    public String getOwner(String identifier) {
        Card card = findByCardIdentifier(identifier);
        if (card == null)
            throw new CustomNotFoundException("Получатель не найден", "receiver");
        Client client = card.getClient();
        if (identifier.matches("^(\\+?7|8)\\d{10}$"))
            return String.format("%s (****%s)", client.getShortenFullName(), card.getLast4());
        return client.getShortenFullName();
    }
}

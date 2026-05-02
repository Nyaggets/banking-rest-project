package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.UserAttempts;
import com.banking.Banking.validation.CardNotFoundException;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadFactory;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Service
@Transactional
public class CardService {
    private final CardRepository repository;
    private final ClientService clientService;
    private final String keySet;
    private final Integer DETAILS_REVEAL_ATTEMPTS = 3;
    @Setter
    private Clock clock;
    private ScheduledExecutorService timerService = Executors.newSingleThreadScheduledExecutor();
    private Map<Long, UserAttempts> revealCount = new ConcurrentHashMap<>();

    public CardService(Clock clock, CardRepository repository, ClientService clientService, @Value("${TINK_KEYSET_BASE64}") String keySet) {
        this.clock = clock;
        this.repository = repository;
        this.clientService = clientService;
        this.keySet = keySet;
    }

    public String encodeString(String line, Long clientId) {
        try {
            AeadConfig.register();
            byte[] keysetJsonBytes = Base64.getDecoder().decode(keySet);
            KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(keysetJsonBytes));
            Aead aead = AeadFactory.getPrimitive(keysetHandle);

            byte[] cipherText = aead.encrypt(line.getBytes(), clientId.toString().getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Ошибка шифрования данных " + e.getMessage());
        }
    }

    public String decodeString(String lineBase64, Long clientId) {
        try {
            AeadConfig.register();
            byte[] keysetJsonBytes = Base64.getDecoder().decode(keySet);
            KeysetHandle keysetHandle = CleartextKeysetHandle.read(JsonKeysetReader.withBytes(keysetJsonBytes));
            Aead aead = AeadFactory.getPrimitive(keysetHandle);

            byte[] cipherText = Base64.getDecoder().decode(lineBase64);
            var decodedLine = aead.decrypt(cipherText, clientId.toString().getBytes());
            return new String(decodedLine);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Ошибка дешифрования данных");
        }
    }

    public String generateSha256Hash(String line) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Ошибка генерации данных карты");
        }
        byte[] hashBytes = digest.digest(line.getBytes());
        StringBuilder resultString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) resultString.append("0");
            resultString.append(hex);
        }
        return resultString.toString();
    }

    public String generateCardNumber(){
        StringBuilder numberBase = new StringBuilder("123456");
        Random random = new Random();
        for (int i = 0; i < 14; i++){
            numberBase.append(random.nextInt(10));
        }
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
        Client client = clientService.findById(clientId);
        if (client == null)
            throw new EntityNotFoundException("Пользователь не найден");

        String cardNumber = generateCardNumber();
        while (repository.findByCardNumber(cardNumber).isPresent())
            cardNumber = generateCardNumber();

        String cvv = generateCVV();
        while (repository.findByCVV(cvv).isPresent())
            cvv = generateCVV();

        Card card = Card.builder()
                        .client(client)
                        .clientName(client.getName())
                        .cardNumber(encodeString(cardNumber, clientId))
                        .cvv(encodeString(cvv, clientId))
                        .balance(BigDecimal.ZERO)
                        .createdDate(LocalDate.now())
                        .expiredDate(LocalDate.now().plusYears(7))
                        .last4(cardNumber.substring(16))
                        .cardNumberHash(generateSha256Hash(cardNumber))
                        .cvvHash(generateSha256Hash(cvv))
                        .build();
        return repository.save(card);
    }

    public Card findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Card findByIdOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена"));
    }

    public Card findByCardNumberHash(String number) {
        String numberHash = generateSha256Hash(number);
        return repository.findByCardNumberHash(numberHash).orElse(null);
    }

    public Card findHiddenNumberHash(String last4) {
        String numberHash = generateSha256Hash(last4);
        return repository.findHiddenNumber(numberHash).orElse(null);
    }

    public Card findByCardNumberOrThrow(String number) {
        return repository.findByCardNumber(number)
                .orElseThrow(() -> new CardNotFoundException("Карта не найдена по номеру"));
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
            card = findHiddenNumberHash(identifier);
        else
            card = findById(Long.valueOf(identifier));
        return card;
    }

    public List<Card> findByClientId(Long id){
        if (clientService.findById(id) == null){
            throw new EntityNotFoundException("Пользователь не найден");
        }
        return repository.findAllByClientId(id).stream()
                .sorted(Comparator.comparing(Card::getBalance).reversed())
                .toList();
    }

    public boolean deleteCard(Long id){
        if (repository.findById(id).orElse(null) == null){
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    public Map<String, String> revealCardDetails(Long clientId, String password, Long cardId){
        Instant attemptTime = clock.instant();
        UserAttempts userAttempts = revealCount.compute(clientId, (id, current) -> {
            if (current == null || attemptTime.isAfter(current.expiresAt()))
                return new UserAttempts(DETAILS_REVEAL_ATTEMPTS, attemptTime.plus(1, ChronoUnit.HOURS));
            return current;
        });

        if (userAttempts.attemptsLeft() == 0)
            throw new RuntimeException("Лимит попыток исчерпан. Попробуйте ещё раз после " + userAttempts.expiresAt());

        if (!clientService.checkPassword(password, clientId)) {
            revealCount.computeIfPresent(clientId, (id, current) ->
                    new UserAttempts(current.attemptsLeft() - 1, current.expiresAt()));
            throw new BadCredentialsException("Неверный пароль. Осталось попыток: " + userAttempts.attemptsLeft());
        }

        Card card = findByIdOrThrow(cardId);
        return new HashMap<>() {{
            put("cvv", decodeString(card.getCvv(), clientId));
            put("cardNumber", decodeString(card.getCardNumber(), clientId));
        }};
    }
}

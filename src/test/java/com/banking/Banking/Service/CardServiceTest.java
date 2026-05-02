package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    private CardService cardService;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private ClientService clientService;
    private Card card;
    private Long clientId;
    private Clock testClock;

    @BeforeEach
    void setUp() {
        testClock = Clock.fixed(Instant.parse("2026-02-05T10:00:00Z"), ZoneId.of("UTC"));
        cardService = new CardService(testClock, cardRepository, clientService, "ewogICAgInByaW1hcnlLZXlJZCI6IDIwMDczMzQ5MjAsCiAgICAia2V5IjogW3sKICAgICAgICAia2V5RGF0YSI6IHsKICAgICAgICAgICAgInR5cGVVcmwiOiAidHlwZS5nb29nbGVhcGlzLmNvbS9nb29nbGUuY3J5cHRvLnRpbmsuQWVzRWF4S2V5IiwKICAgICAgICAgICAgImtleU1hdGVyaWFsVHlwZSI6ICJTWU1NRVRSSUMiLAogICAgICAgICAgICAidmFsdWUiOiAiRWdJSUVCb2d1VDEwWU9Iek5hbTdETkRzbUJjMGdaejRNZlFHTVBNVERKM3RJbmlpUkV3PSIKICAgICAgICB9LAogICAgICAgICJvdXRwdXRQcmVmaXhUeXBlIjogIlRJTksiLAogICAgICAgICJrZXlJZCI6IDIwMDczMzQ5MjAsCiAgICAgICAgInN0YXR1cyI6ICJFTkFCTEVEIgogICAgfV0KfQ==");

        clientId = 1L;
        card = Card.builder()
                .id(1L)
                .cardNumber(cardService.encodeString("11111111111111111111", clientId))
                .cvv(cardService.encodeString("111", clientId))
                .build();
    }

    @Test
    void createCard_GenerateUniqueNumber() {
        String existingNumber = "11111111111111111111";
        String newNumber = "22222222222222222222";
        Long clientId = 1L;
        CardService cardServiceSpy = spy(cardService);
        Client mockClient = new Client();

        when(clientService.findById(clientId)).thenReturn(mockClient);
        doReturn(existingNumber)
                .doReturn(newNumber)
                .when(cardServiceSpy).generateCardNumber();
        when(cardRepository.findByCardNumber(anyString()))
                .thenReturn(Optional.of(new Card()))
                .thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardServiceSpy.createCard(clientId);

        verify(cardServiceSpy, times(2)).generateCardNumber();
        assertEquals(result.getCardNumberHash(), cardServiceSpy.generateSha256Hash(newNumber));
    }

    @Test
    void createCard_GenerateUniqueCVV() {
        String existingCVV = "111";
        String newCVV = "222";
        Long clientId = 1L;
        CardService cardServiceSpy = spy(cardService);
        Client mockClient = new Client();

        when(clientService.findById(clientId)).thenReturn(mockClient);
        doReturn(existingCVV)
                .doReturn(newCVV)
                .when(cardServiceSpy).generateCVV();
        when(cardRepository.findByCVV(anyString()))
                .thenReturn(Optional.of(new Card()))
                .thenReturn(Optional.empty());
        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardServiceSpy.createCard(clientId);

        verify(cardServiceSpy, times(2)).generateCVV();
        assertEquals(result.getCvvHash(), cardServiceSpy.generateSha256Hash(newCVV));
    }

    @Test
    public void RevealAttempts_Success() {
        when(clientService.checkPassword(anyString(), anyLong())).thenReturn(true);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        cardService.revealCardDetails(clientId, "password", card.getId());
        Map<String, String> details = cardService.revealCardDetails(clientId, "password", card.getId());

        assertEquals(details.get("cardNumber"), cardService.decodeString(card.getCardNumber(), clientId));
        assertEquals(details.get("cvv"), cardService.decodeString(card.getCvv(), clientId));
    }

    @Test
    public void RevealAttempts_TooManyTries() {
        when(clientService.checkPassword(anyString(), anyLong())).thenReturn(false);

        for (int i = 0; i < 3; i++)
            assertThrows(BadCredentialsException.class, () -> cardService.revealCardDetails(clientId, "", card.getId()));

        assertThrows(RuntimeException.class, () -> cardService.revealCardDetails(clientId, "", card.getId()));
    }

    @Test
    public void RevealAttempts_TimeLimitTest() {
        when(clientService.checkPassword(anyString(), anyLong())).thenReturn(false);
        for (int i = 0; i < 3; i++)
            assertThrows(BadCredentialsException.class, () -> cardService.revealCardDetails(clientId, "", card.getId()));

        assertThrows(RuntimeException.class, () -> cardService.revealCardDetails(clientId, "", card.getId()));

        when(clientService.checkPassword(anyString(), anyLong())).thenReturn(true);
        when(cardRepository.findById(clientId)).thenReturn(Optional.of(card));
        cardService.setClock(Clock.fixed(Instant.parse("2026-02-05T12:00:01Z"), ZoneId.of("UTC")));
        Map<String, String> details = cardService.revealCardDetails(clientId, "", card.getId());

        assertEquals(details.get("cardNumber"), cardService.decodeString(card.getCardNumber(), clientId));
        assertEquals(details.get("cvv"), cardService.decodeString(card.getCvv(), clientId));
    }
}

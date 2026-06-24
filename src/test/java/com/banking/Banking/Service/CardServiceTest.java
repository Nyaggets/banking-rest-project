package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import com.banking.Banking.validation.CustomNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {

    @Mock
    private CardRepository repository;
    @Mock
    private ClientService clientService;
    @Mock
    private EncodeService encodeService;
    @Mock
    private VerifyIdentityService identityService;

    @InjectMocks
    private CardService cardService;

    private Client client;
    private Card card;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("Ivan")
                .surname("Ivanov")
                .patronymic("Ivanovich")
                .build();

        card = Card.builder()
                .id(10L)
                .client(client)
                .cardNumber("encNum")
                .cvv("encCvv")
                .last4("1234")
                .balance(BigDecimal.ZERO)
                .createdDate(LocalDate.now())
                .expiredDate(LocalDate.now().plusYears(7))
                .build();
    }

    @Test
    void createCard_Success() {
        when(clientService.findByIdOrThrow(1L)).thenReturn(client);
        when(repository.findByCardNumber(anyString())).thenReturn(Optional.empty());
        when(repository.findByCVV(anyString())).thenReturn(Optional.empty());
        when(encodeService.encodeString(anyString(), anyLong())).thenReturn("encodedValue");
        when(encodeService.generateSha256Hash(anyString())).thenReturn("hashValue");
        when(repository.save(any())).thenReturn(card);

        Card created = cardService.createCard(1L);

        assertNotNull(created);
        verify(repository, times(1)).save(any());
    }

    @Test
    void createCard_ClientNotFound() {
        when(clientService.findByIdOrThrow(1L))
                .thenThrow(new CustomNotFoundException("Пользователь не найден", "client"));

        assertThrows(CustomNotFoundException.class, () -> cardService.createCard(1L));
        verify(repository, never()).save(any());
    }

    @Test
    void belongsToClient_True() {
        assertDoesNotThrow(() -> cardService.belongsToClientOrThrow(1L, card));
    }

    @Test
    void belongsToClient_False() {
        Client other = Client.builder().id(2L).build();
        Card otherCard = Card.builder().id(10L).client(other).build();

        assertThrows(AccessDeniedException.class, () -> cardService.belongsToClientOrThrow(1L, otherCard));
    }

    @Test
    void findByCardIdentifier_ByPhone_Success() {
        String phone = "+79001234567";
        when(clientService.findByPhone(phone)).thenReturn(client);
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(repository.findAllByClientId(anyLong())).thenReturn(List.of(card));

        Card found = cardService.findByCardIdentifier(phone);

        assertNotNull(found);
        assertEquals(client.getId(), found.getClient().getId());
    }

    @Test
    void findByCardIdentifier_InvalidFormat() {
        assertThrows(NumberFormatException.class, () -> cardService.findByCardIdentifier("invalidId"));
    }

    @Test
    void revealCardDetails_Success() {
        when(repository.findById(10L)).thenReturn(Optional.of(card));
        when(clientService.checkPassword("1234", 1L)).thenReturn(true);
        doNothing().when(identityService).throwIfPasswordAttemptLimit(anyLong(), eq(true));
        when(encodeService.decodeString(anyString(), anyLong())).thenReturn("decodedValue");

        Map<String, String> details = cardService.revealCardDetails(1L, "1234", 10L);

        assertEquals("decodedValue", details.get("cvv"));
        assertEquals("decodedValue", details.get("cardNumber"));
    }

    @Test
    void revealCardDetails_AccessDenied() {
        when(repository.findById(10L)).thenReturn(Optional.of(card));

        assertThrows(AccessDeniedException.class, () -> cardService.revealCardDetails(99L, "1234", 10L));
    }

    @Test
    void getOwner_ByPhone_Success() {
        String phone = "+79001234567";
        when(clientService.findByPhone(phone)).thenReturn(client);
        when(clientService.findByIdOrThrow(anyLong())).thenReturn(client);
        when(repository.findAllByClientId(anyLong())).thenReturn(List.of(card));

        String owner = cardService.getOwner(phone);

        assertEquals("Ivanov I. I. (****1234)", owner);
    }

    @Test
    void getOwner_CardNotFound() {
        when(clientService.findByPhone(anyString())).thenReturn(client);

        assertThrows(CustomNotFoundException.class, () -> cardService.getOwner("+79001234567"));
    }
}
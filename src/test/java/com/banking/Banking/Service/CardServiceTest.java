package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private ClientService clientService;
    @InjectMocks
    private CardService cardService;

    @Test
    void whenExistingNumber_GenerateNew() {
        String existingNumber = "12345634569852347829";
        String newNumber = "12345678901234567890";

        CardService cardServiceSpy = spy(cardService);

        Client mockClient = new Client();
        when(clientService.findById(1L)).thenReturn(mockClient);

        doReturn(existingNumber)
                .doReturn(newNumber)
                .when(cardServiceSpy).generateCardNumber();

        when(cardRepository.findByCardNumber(anyString()))
                .thenReturn(Optional.of(new Card()))
                .thenReturn(Optional.empty());

        when(cardRepository.save(any(Card.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Card result = cardServiceSpy.createCard(1L);

        verify(cardServiceSpy, times(2)).generateCardNumber();
        assertThat(result.getCardNumber()).isEqualTo(newNumber);
        assertThat(result.getCardNumber()).hasSize(20);
    }
}

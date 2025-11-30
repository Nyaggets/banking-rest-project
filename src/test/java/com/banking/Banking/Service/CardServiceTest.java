package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Repository.CardRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardServiceTest {
    @Mock
    private CardRepository cardRepository;
    @InjectMocks
    private CardService cardService;

    @Test
    void whenExistingNumber_GenerateNew() {
        String existingNumber = "12345634569852347829";
        String uniqueNumber = "12345676898639876297";

        CardService cardServiceSpy = spy(cardService);

        doReturn(existingNumber)
                .doReturn(uniqueNumber)
                .when(cardServiceSpy).generateCardNumber();

        when(cardRepository.findByCardNumber(existingNumber))
                .thenReturn(Optional.of(new Card()));

        Card newCard = cardServiceSpy.createCard(new Card());

        verify(cardServiceSpy, times(2)).generateCardNumber();
        verify(cardRepository).save(any(Card.class));
        assert(newCard.getCardNumber()).equals(uniqueNumber);
    }
}

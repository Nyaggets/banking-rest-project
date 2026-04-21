package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.CardRepository;
import com.banking.Banking.Repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private CardRepository cardRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private ClientService clientService;

    @Test
    void LoadByUsernameTest_Success() {
        final String username = "user";
        final String cardNumber = "11111111111111111111";
        Client client = new Client();
        client.setId(1L);
        client.setUsername(username);
        Card card = Card.builder().cardNumber(cardNumber).clientName(username).build();

        when(clientRepository.findByUsername(username)).thenReturn(Optional.of(client));
        when(cardRepository.findByCardNumber(cardNumber)).thenReturn(Optional.of(card));

        UserDetails detailsByUsername = clientService.loadUserByUsername(username);
        UserDetails detailsByCardNumber = clientService.loadUserByUsername(cardNumber);

        assertEquals(username, detailsByUsername.getUsername());
        assertEquals(username, detailsByCardNumber.getUsername());
    }
}

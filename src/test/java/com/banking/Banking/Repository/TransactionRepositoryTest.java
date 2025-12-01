package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TransactionRepositoryTest {
    @Autowired
    TransactionRepository transactionRepository;
    @Autowired
    CardRepository cardRepository;

    @Test
    void findByCardNumber() {
        Card firstCard = new Card();
        firstCard.setCardNumber("12345634569852347829");
        cardRepository.save(firstCard);

        Card secondCard = new Card();
        secondCard.setCardNumber("12345634569852346535");
        cardRepository.save(secondCard);

        Transaction transactionOne = new Transaction();
        transactionOne.setSenderCard(firstCard);
        transactionOne.setReceiverCard(secondCard);
        transactionRepository.save(transactionOne);

        Transaction transactionTwo = new Transaction();
        transactionTwo.setSenderCard(secondCard);
        transactionTwo.setReceiverCard(firstCard);
        transactionRepository.save(transactionTwo);

        assertThat(transactionRepository.findByCardId(firstCard.getId()).size()).isEqualTo(2);
    }
}

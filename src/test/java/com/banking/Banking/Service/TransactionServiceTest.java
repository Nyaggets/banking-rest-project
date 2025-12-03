package com.banking.Banking.Service;

import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void findByCardNumberPageable() {
        List<Transaction> transactions = List.of(
                new Transaction(),
                new Transaction()
        );
        Pageable pageable = PageRequest.of(0, 5);
        Page<Transaction> expectedPage = new PageImpl<>(transactions, pageable, 10);

        when(transactionRepository.findByCardId(1L, pageable)).thenReturn(expectedPage);

        Page<Transaction> resultPage = transactionService.findByCardId(1L, pageable);

        assertThat(resultPage.getContent()).hasSize(2);
        assertThat(resultPage.getTotalElements()).isEqualTo(10);
    }
}

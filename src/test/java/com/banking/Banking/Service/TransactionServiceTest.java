package com.banking.Banking.Service;

import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Repository.CardRepository;
import com.banking.Banking.Repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void findByCardNumber() {
        List<Transaction> transactions = List.of(
                new Transaction(),
                new Transaction()
        );
        when(transactionRepository.findByCardId(anyLong())).thenReturn(transactions);
        List<Transaction> resultPage = transactionService.findByCardId(1L);

        assertThat(resultPage).hasSize(2);
    }
}

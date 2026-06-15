package com.banking.Banking.Controller;

import com.banking.Banking.Dto.DepositDtoRequest;
import com.banking.Banking.Dto.TransferDtoRequest;
import com.banking.Banking.Dto.WithdrawalDtoRequest;
import com.banking.Banking.Entity.CounterpartyTypeEnum;
import com.banking.Banking.Entity.OperationTypeEnum;
import com.banking.Banking.Entity.SessionUser;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.TransactionService;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/cards")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionMapper mapper;

    /**
     * Расчет суммы комиссии для перевода по сумме операции
     */
    @PostMapping("/transactions/transfer/commission")
    public ResponseEntity<?> calculateTransferCommission(@RequestParam String amount) {
        if (amount.isEmpty())
            return ResponseEntity.ok(BigDecimal.ZERO);
        var commissionAmount = transactionService.calculateCommission(new BigDecimal(amount), OperationTypeEnum.TRANSFER_OUT);
        return ResponseEntity.ok(Map.of("commission", commissionAmount));
    }

    /**
     * Создание операции перевода между картами клиентов
     */
    @PostMapping("/{cardId}/transactions/transfer")
    public ResponseEntity<?> createTransfer(@Valid @RequestBody TransferDtoRequest dtoRequest, Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Transaction transfer = transactionService.createTransferToInternalClient(client.getId(), dtoRequest);
        return new ResponseEntity<>(Map.of("operationId", transfer.getId()), HttpStatus.CREATED);
    }

    /**
     * Создание операции пополнения на карту клиента
     */
    @PostMapping("/{cardId}/transactions/deposit")
    public ResponseEntity<?> createExternalDeposit(@Valid @RequestBody DepositDtoRequest depositDto) {
        Transaction deposit = transactionService.createDeposit(depositDto, CounterpartyTypeEnum.EMPLOYER);
        return new ResponseEntity<>(Map.of("operationId", deposit.getId()), HttpStatus.CREATED);
    }

    /**
     * Создание операции пополнения баланса номера телефона с карты клиента
     */
    @PostMapping("/{cardId}/transactions/balance-top-up")
    public ResponseEntity<?> createBalanceTopUp(@Valid @RequestBody WithdrawalDtoRequest withdrawalDto, Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Transaction withdrawal = transactionService.createBalanceTopUp(client.getId(), withdrawalDto);
        return new ResponseEntity<>(Map.of("operationId", withdrawal.getId()), HttpStatus.CREATED);
    }

    /**
     * Создание операции покупки с карты клиента
     */
    @PostMapping("/{cardId}/transactions/purchase")
    public ResponseEntity<?> createPurchase(@Valid @RequestBody WithdrawalDtoRequest withdrawalDto, Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Transaction withdrawal = transactionService.createWithdrawal(client.getId(), withdrawalDto, CounterpartyTypeEnum.PURCHASE);
        return new ResponseEntity<>(Map.of("operationId", withdrawal.getId()), HttpStatus.CREATED);
    }

    /**
     * Получение деталей конкретной транзакции текущего клиента
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> transactionDetails(@RequestParam Long operationId, Authentication auth) throws AccessDeniedException {
        var transaction = transactionService.findById(operationId, auth);
        return ResponseEntity.ok(mapper.toDto(transaction));
    }

    /**
     * Получение страницы из списка транзакций текущего клиента
     */
    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<Page> history(Authentication auth, @RequestParam(defaultValue = "0") int page,
                                        @Nullable @RequestParam Long cardId,
                                        @Nullable @RequestParam List<OperationTypeEnum> types,
                                        @Nullable @RequestParam String start,
                                        @Nullable @RequestParam String end) throws AccessDeniedException {
        SessionUser client = (SessionUser) auth.getPrincipal();
        var transactions = transactionService.findTransactions(client.getId(), page, types, cardId, start, end);
        var dtos = mapper.toDtoList(transactions.getContent());
        var dtoPage = new PageImpl<>(dtos, transactions.getPageable(), transactions.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }
}

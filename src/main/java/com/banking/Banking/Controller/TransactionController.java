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

    @PostMapping("/{cardId}/transactions/commission")
    public ResponseEntity<?> calculateCommission(@RequestParam String amount) {
        if (amount.isEmpty())
            return ResponseEntity.ok(BigDecimal.ZERO);
        var commissionAmount = transactionService.calculateCommission(new BigDecimal(amount));
        return ResponseEntity.ok(Map.of("commission", commissionAmount));
    }

    @PostMapping("/{cardId}/transactions/transfer")
    public ResponseEntity<?> createTransfer(@Valid @RequestBody TransferDtoRequest dtoRequest, Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Transaction transfer = transactionService.createTransferToInternalClient(client.getId(), dtoRequest);
        return new ResponseEntity<>(Map.of("operationId", transfer.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/{cardId}/transactions/deposit")
    public ResponseEntity<?> createExternalDeposit(@Valid @RequestBody DepositDtoRequest depositDto) {
        Transaction deposit = transactionService.createDeposit(depositDto, CounterpartyTypeEnum.EMPLOYER);
        return new ResponseEntity<>(Map.of("operationId", deposit.getId()), HttpStatus.CREATED);
    }

//    @PostMapping("/{cardId}/transactions/deposit")
//    public ResponseEntity<?> createDeposit(@Valid @RequestBody DepositDtoRequest depositDto) {
//        Transaction deposit = transactionService.createDeposit(depositDto);
//        return new ResponseEntity<>(Map.of("operationId", deposit.getId()), HttpStatus.CREATED);
//    }

//    @PostMapping("/{cardId}/transactions/withdrawal")
//    public ResponseEntity<?> createWithdrawal(@Valid @RequestBody WithdrawalDtoRequest withdrawalDto, Authentication auth) {
//        SessionUser client = (SessionUser) auth.getPrincipal();
//
//        Transaction withdrawal = transactionService.createWithdrawal(client.getId(), withdrawalDto);
//        return new ResponseEntity<>(Map.of("operationId", withdrawal.getId()), HttpStatus.CREATED);
//    }

    @PostMapping("/{cardId}/transactions/balance-top-up")
    public ResponseEntity<?> createBalanceTopUp(@Valid @RequestBody WithdrawalDtoRequest withdrawalDto, Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Transaction withdrawal = transactionService.createBalanceTopUp(client.getId(), withdrawalDto);
        return new ResponseEntity<>(Map.of("operationId", withdrawal.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/{cardId}/transactions/purchase")
    public ResponseEntity<?> createPurchase(@Valid @RequestBody WithdrawalDtoRequest withdrawalDto, Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Transaction withdrawal = transactionService.createWithdrawal(client.getId(), withdrawalDto, CounterpartyTypeEnum.PURCHASE);
        return new ResponseEntity<>(Map.of("operationId", withdrawal.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<?> slice(Authentication auth) {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok(transactionService.findTransactions(client.getId(), 0));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> transactionDetails(@RequestParam Long operationId, Authentication auth) throws AccessDeniedException {
        var transaction = transactionService.findById(operationId, auth);
        return ResponseEntity.ok(mapper.toDto(transaction));
    }

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

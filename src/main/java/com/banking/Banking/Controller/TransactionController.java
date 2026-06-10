package com.banking.Banking.Controller;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Entity.OperationTypes;
import com.banking.Banking.Entity.SessionUser;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.TransactionService;
import com.banking.Banking.validation.CustomException;
import com.banking.Banking.validation.DepositGroup;
import com.banking.Banking.validation.TransferGroup;
import com.banking.Banking.validation.WithdrawalGroup;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/cards")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionMapper mapper;

    private String mapFieldName(String field) {
        return switch (field) {
            case "receiverIdentifier" -> "receiver";
            case "clientCardId" -> "sender";
            default -> field;
        };
    }

    private Map<String, String> validateBindingResult(BindingResult result) {
        if (result.hasErrors()) {
            return result.getFieldErrors().stream().collect(
                    Collectors.toMap(
                            error -> mapFieldName(error.getField()),
                            FieldError::getDefaultMessage
                    ));
        }
        return null;
    }

    @PostMapping("/{cardId}/transactions/commission")
    public ResponseEntity<?> calculateCommission(@RequestParam String amount) {
        if (amount.isEmpty())
            return ResponseEntity.ok(BigDecimal.ZERO);
        var commissionAmount = transactionService.calculateCommission(new BigDecimal(amount));
        return ResponseEntity.ok(Map.of("commission", commissionAmount));
    }

    @PostMapping("/{cardId}/transactions/transfer")
    public ResponseEntity<?> createTransfer(@Validated(TransferGroup.class)
                                            @RequestBody TransactionDtoRequest dtoRequest,
                                            BindingResult result) {
        var errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            throw new CustomException("VALIDATION EXCEPTION", errorResponse);

        Transaction transfer = transactionService.createTransfer(dtoRequest);
        return new ResponseEntity<>(Map.of("operationId", transfer.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/{cardId}/transactions/deposit")
    public ResponseEntity<?> createDeposit(@Validated(DepositGroup.class)
                                            @RequestBody TransactionDtoRequest depositDto,
                                            BindingResult result) {
        var errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            throw new CustomException("VALIDATION EXCEPTION", errorResponse);

        Transaction deposit = transactionService.createDeposit(depositDto);
        return new ResponseEntity<>(Map.of("operationId", deposit.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/{cardId}/transactions/withdrawal")
    public ResponseEntity<?> createWithdrawal(@Validated(WithdrawalGroup.class)
                                               @RequestBody TransactionDtoRequest withdrawalDto,
                                                BindingResult result) {
        var errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            throw new CustomException("VALIDATION EXCEPTION", errorResponse);

        Transaction withdrawal = transactionService.createWithdrawal(withdrawalDto);
        return new ResponseEntity<>(Map.of("operationId", withdrawal.getId()), HttpStatus.CREATED);
    }

    @PostMapping("/{cardId}/transactions/balance-deposit")
    public ResponseEntity<?> balanceDeposit(@Validated(WithdrawalGroup.class)
                                              @RequestBody TransactionDtoRequest withdrawalDto,
                                              BindingResult result) {
        var errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            throw new CustomException("VALIDATION EXCEPTION", errorResponse);

        Transaction withdrawal = transactionService.balanceDeposit(withdrawalDto);
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
                                        @Nullable @RequestParam List<OperationTypes> types,
                                        @Nullable @RequestParam String start,
                                        @Nullable @RequestParam String end) throws AccessDeniedException {
        SessionUser client = (SessionUser) auth.getPrincipal();
        var transactions = transactionService.findTransactions(client.getId(), page, types, cardId, start, end);
        var dtos = mapper.toDtoList(transactions.getContent());
        var dtoPage = new PageImpl<>(dtos, transactions.getPageable(), transactions.getTotalElements());
        return ResponseEntity.ok(dtoPage);
    }
}

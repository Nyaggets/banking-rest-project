package com.banking.Banking.Controller;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.Service.TransactionService;
import com.banking.Banking.validation.DepositGroup;
import com.banking.Banking.validation.TransferGroup;
import com.banking.Banking.validation.WithdrawalGroup;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/cards")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionMapper mapper;
    @Autowired
    private ClientService clientService;
    @Autowired
    private CardService cardService;

    private String mapFieldName(String field) {
        return switch (field) {
            case "receiverIdentifier" -> "receiver";
            case "senderCardId" -> "sender";
            default -> field;
        };
    }

    private ResponseEntity<Map<Object, String>> validateBindingResult(BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().stream().collect(
                            Collectors.toMap(
                                    error -> mapFieldName(error.getField()),
                                    FieldError::getDefaultMessage
                            ))
            );
        }
        return null;
    }

    @PostMapping("/{cardId}/transactions/commission")
    public ResponseEntity<?> calculateCommission(@RequestParam String amount) {
        if (amount.isEmpty())
            return ResponseEntity.ok(BigDecimal.ZERO);
        try {
            var commissionAmount = transactionService.calculateCommission(new BigDecimal(amount));
            return ResponseEntity.ok(commissionAmount);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{cardId}/transactions/transfer")
    public ResponseEntity<?> createTransfer(@Validated(TransferGroup.class)
                                            @RequestBody TransactionDtoRequest dtoRequest,
                                            BindingResult result) {
        ResponseEntity<Map<Object, String>> errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            return ResponseEntity.badRequest().body(errorResponse);

        transactionService.createTransfer(dtoRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{cardId}/transactions/deposit")
    public ResponseEntity<?> createDeposit(@Validated(DepositGroup.class)
                                            @RequestBody TransactionDtoRequest depositDto,
                                            BindingResult result) {
        ResponseEntity<Map<Object, String>> errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            return ResponseEntity.badRequest().body(errorResponse);

        try {
            transactionService.createDeposit(depositDto);
            return ResponseEntity.ok().build();
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/{cardId}/transactions/withdrawal")
    public ResponseEntity<?> createWithdrawal(@Validated(WithdrawalGroup.class)
                                               @RequestBody TransactionDtoRequest withdrawalDto,
                                                BindingResult result) {
        ResponseEntity<Map<Object, String>> errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            return ResponseEntity.badRequest().body(errorResponse);

        try {
            transactionService.createWithdrawal(withdrawalDto);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/{cardId}/transactions/{clientId}")
    public ResponseEntity<?> slice(@PathVariable Long clientId) {
        Pageable pageable = PageRequest.of(0, 2);
        return ResponseEntity.ok(transactionService.findTransactions(clientId, 0));
    }

    @GetMapping("/transactions")
    public ResponseEntity<?> transactionDetails(@RequestParam Long operationId, Authentication auth) throws AccessDeniedException {
        var transaction = transactionService.findById(operationId, auth);
        return ResponseEntity.ok(mapper.toDto(transaction));
    }
}

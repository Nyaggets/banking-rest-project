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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cards/{cardId}/transactions")
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

    @PostMapping("/commission")
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

    @PostMapping("/transfer")
    public ResponseEntity<?> createTransfer(@Validated(TransferGroup.class)
                                            @RequestBody TransactionDtoRequest dtoRequest,
                                            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().stream().collect(
                            Collectors.toMap(
                                    error -> mapFieldName(error.getField()),
                                    FieldError::getDefaultMessage
                            ))
            );
        }
        try {
            var validTransaction = transactionService.transferValidation(dtoRequest);
            if (!validTransaction.isEmpty())
                return ResponseEntity.badRequest().body(validTransaction);

            transactionService.createTransfer(dtoRequest);
            return ResponseEntity.ok().build();
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> createDeposit(@Validated(DepositGroup.class)
                                            @RequestBody TransactionDtoRequest depositDto,
                                            BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().stream().collect(
                            Collectors.toMap(
                                    error -> mapFieldName(error.getField()),
                                    FieldError::getDefaultMessage
                            ))
            );
        }
        try {
            var validTransaction = transactionService.depositValidation(depositDto);
            if (validTransaction.isEmpty()) {
                transactionService.createDeposit(depositDto);
                return ResponseEntity.ok().build();
            }

            if (validTransaction.containsKey("unauthorized"))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validTransaction);
            if (validTransaction.containsKey("forbidden"))
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(validTransaction);
            return ResponseEntity.badRequest().body(validTransaction);
        }
        catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/withdrawal")
    public ResponseEntity<?> createWithdrawal(@Validated(WithdrawalGroup.class)
                                               @RequestBody TransactionDtoRequest withdrawalDto,
                                                BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().stream().collect(
                            Collectors.toMap(
                                    error -> mapFieldName(error.getField()),
                                    FieldError::getDefaultMessage
                            ))
            );
        }
        try {
            var validTransaction = transactionService.withdrawalValidation(withdrawalDto);
            if (!validTransaction.isEmpty())
                return ResponseEntity.badRequest().body(validTransaction);

            transactionService.createWithdrawal(withdrawalDto);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping
    public List<TransactionDtoResponse> findAllByCardId(@PathVariable Long cardId){
        return mapper.toDtoList(transactionService.findByCardId(cardId));
    }
}

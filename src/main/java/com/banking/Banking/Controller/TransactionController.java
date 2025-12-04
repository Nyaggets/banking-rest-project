package com.banking.Banking.Controller;

import com.banking.Banking.Dto.TransactionDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cards/{cardId}/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionMapper mapper;

    @PostMapping("/create-transfer")
    public ResponseEntity<TransactionDtoResponse> createTransfer(@PathVariable Long cardId,
                                                                 @RequestBody TransactionDtoRequest transactionDtoRequest){
        Transaction transaction = mapper.fromDtoRequest(transactionDtoRequest);
        if (transactionService.createTransfer(transaction, cardId) == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(transaction));
    }

    @PostMapping("/create-replenish")
    public ResponseEntity<TransactionDtoResponse> createReplenish(@PathVariable Long cardId,
                                                                 @RequestBody TransactionDtoRequest transactionDtoRequest){
        Transaction transaction = mapper.fromDtoRequest(transactionDtoRequest);
        if (transactionService.createReplenish(transaction, cardId) == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(transaction));
    }

    @PostMapping("/create-write-off")
    public ResponseEntity<TransactionDtoResponse> createWriteOff(@PathVariable Long cardId,
                                                                 @RequestBody TransactionDtoRequest transactionDtoRequest){
        Transaction transaction = mapper.fromDtoRequest(transactionDtoRequest);
        if (transactionService.createWriteOff(transaction, cardId) == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(transaction));
    }

    @GetMapping
    public Page<TransactionDtoResponse> findAllByCardId(@PathVariable Long cardId,
                                                        @PageableDefault(size = 5) Pageable pageable){
        return transactionService.findByCardId(cardId, pageable).map(transaction -> mapper.toDtoResponse(transaction));
    }
}

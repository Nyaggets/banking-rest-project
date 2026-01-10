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

import java.util.List;

@RestController
@RequestMapping("/cards/{cardId}/transactions")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private TransactionMapper mapper;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionDtoResponse> createTransfer(@PathVariable String cardId,
                                                                 @RequestBody TransactionDtoRequest transactionDtoRequest){
        Transaction transaction = mapper.fromDto(transactionDtoRequest);
        if (transactionService.createTransfer(transaction, Long.valueOf(cardId)) == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDto(transaction));
    }

    @PostMapping("/replenish")
    public ResponseEntity<TransactionDtoResponse> createReplenish(@PathVariable Long cardId,
                                                       @RequestBody TransactionDtoRequest replenishDto){
        Transaction replenish = mapper.fromDto(replenishDto);
        if (transactionService.createReplenish(replenish, cardId) == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDto(replenish));
    }

    @PostMapping("/writeoff")
    public ResponseEntity<TransactionDtoResponse> createWriteOff(@PathVariable Long cardId,
                                                      @RequestBody TransactionDtoRequest writeOffDto){
        Transaction writeOff = mapper.fromDto(writeOffDto);
        if (transactionService.createWriteOff(writeOff, cardId) == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDto(writeOff));
    }

    @GetMapping
    public List<TransactionDtoResponse> findAllByCardId(@PathVariable Long cardId){
        return mapper.toDtoList(transactionService.findByCardId(cardId));
    }
}

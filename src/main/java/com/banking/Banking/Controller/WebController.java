package com.banking.Banking.Controller;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.Service.TransactionService;
import com.banking.Banking.validation.RequestLimitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class WebController {
    private final CardService cardService;
    private final ClientService clientService;
    private final TransactionService transactionService;
    private final CardMapper cardMapper;
    private final ClientMapper clientMapper;
    private final TransactionMapper transactionMapper;
    @Autowired
    public WebController(CardService cardService, ClientService clientService, TransactionService transactionService,
                         CardMapper cardMapper, ClientMapper clientMapper, TransactionMapper transactionMapper) {
        this.cardService = cardService;
        this.clientService = clientService;
        this.transactionService = transactionService;
        this.cardMapper = cardMapper;
        this.clientMapper = clientMapper;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping("clients/{clientId}/history")
    @ResponseBody
    public ResponseEntity<List<TransactionDtoResponse>> history(@PathVariable Long clientId){
        Client client = clientService.findByIdOrThrow(clientId);
        List<Transaction> transactions = transactionService.findByClientId(clientId);

        return ResponseEntity.ok(transactionMapper.toDtoList(transactions));
    }

    @GetMapping("clients/me")
    @ResponseBody
    public ResponseEntity<ClientDtoResponse> getCurrentUser(Authentication auth){
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()){
            return ResponseEntity.notFound().build();
        }
        Client client = clientService.findByUsername(auth.getName());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(clientMapper.toDtoResponse(client));
    }

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/signin")
    public String signinPage(){
        return "signin";
    }

    @GetMapping("/main")
    public String mainPage(){
        return "main";
    }

    @GetMapping("/transfer")
    public String transferPage(){
        return "transfer";
    }

    @GetMapping("/history")
    public String historyPage(){
        return "history";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal){
        Client client = clientService.findByUsername(principal.getName());
        model.addAttribute("client", clientMapper.toDtoResponse(client));
        return "profile";
    }

    @GetMapping("/card")
    public String cardPage(@RequestParam Long id){
        return "card";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("card/{cardId}/card-details")
    public ResponseEntity<?> revealCardDetails(Authentication auth, @PathVariable String cardId,
                                               @RequestBody Map<String, String> requestBody)
                                                throws RequestLimitException, AccessDeniedException {
        Client client = clientService.findByUsername(auth.getName());
        if (client == null)
            throw new BadCredentialsException("Пользователь не найден");
        Map<String, String> details = cardService.revealCardDetails(client.getId(), requestBody.get("password"), Long.valueOf(cardId));
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(details);
    }
}

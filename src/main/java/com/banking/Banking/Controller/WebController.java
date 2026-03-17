package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoRequest;
import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.TransactionDtoResponse;
import com.banking.Banking.Entity.Card;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.Transaction;
import com.banking.Banking.Mapper.CardMapper;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.CardService;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.Service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

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
        Client client = clientService.findById(clientId);
        if (client == null){
            return ResponseEntity.notFound().build();
        }
        List<Transaction> transactions = transactionService.findByClientId(clientId);

        return ResponseEntity.ok(transactionMapper.toDtoList(transactions));
    }

    @GetMapping("clients/me")
    @ResponseBody
    public ResponseEntity<Client> getCurrentUser(Authentication auth){
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()){
            return ResponseEntity.notFound().build();
        }
        Client client = clientService.findByUsername(auth.getName());
        return ResponseEntity.ok(client);
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
        model.addAttribute("client", clientMapper.toDtoResponse(client  ));
        return "profile";
    }

    @GetMapping("/card")
    public String cardPage(@RequestParam Long id){
        return "card";
    }

    @PostMapping("/signin")
    public String signinClient(@ModelAttribute("newClient") ClientDtoRequest newClientDto){
        Client newClient = clientMapper.fromDtoRequest(newClientDto);
        if (!clientService.createClient(newClient)){
            return "signin";
        }
        return "redirect:/login";
    }

    @PostMapping("/login")
    public String loginClient(@RequestBody ClientDtoRequest clientDto){
        Client existingClient = clientMapper.fromDtoRequest(clientDto);
        if (clientService.findByUsername(existingClient.getUsername()) == null){
            return "login";
        }
        return "redirect:/main";
    }
}

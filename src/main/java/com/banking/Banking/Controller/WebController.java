package com.banking.Banking.Controller;

import com.banking.Banking.Dto.CardDtoRequest;
import com.banking.Banking.Dto.ClientDtoRequest;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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

    @GetMapping("/login")
    public String loginPage(Model model){
        ClientDtoRequest existingClient = new ClientDtoRequest();
        model.addAttribute("client", existingClient);
        return "login";
    }

    @GetMapping("/signin")
    public String signinPage(Model model){
        ClientDtoRequest newClient = new ClientDtoRequest();
        model.addAttribute("newClient", newClient);
        return "signin";
    }

    @GetMapping("/main")
    public String mainPage(Model model, Principal principal){
        Client client = clientService.findByUsername(principal.getName());
        List<Card> cards = cardService.findAllByClientId(client.getId());
        List<Transaction> transactionList = cards.stream()
                .flatMap(card -> transactionService.findByCardId(card.getId()).stream())
                .limit(5)
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .toList();

        model.addAttribute("client", clientMapper.toDtoResponse(client));
        model.addAttribute("cards", cardMapper.toListDto(cards));
        model.addAttribute("transactions", transactionMapper.toDtoList(transactionList));
        return "main";
    }

    @GetMapping("/transfer")
    public String transferPage(Model model, Principal principal){
        Client client = clientService.findByUsername(principal.getName());
        List<Card> cards = cardService.findAllByClientId(client.getId());
        Transaction transfer = new Transaction();

        model.addAttribute("cards", cardMapper.toListDto(cards));
        model.addAttribute("newTransfer", transactionMapper.toDto(transfer));
        return "transfer";
    }

    @GetMapping("/history")
    public String historyPage(Model model, Principal principal){
        Client client = clientService.findByUsername(principal.getName());
        List<Card> cards = cardService.findAllByClientId(client.getId());
        List<Transaction> transactionList = cards.stream()
                .flatMap(card -> transactionService.findByCardId(card.getId()).stream())
                .sorted(Comparator.comparing(Transaction::getTimestamp))
                .toList();

        model.addAttribute("transactions", transactionMapper.toDtoList(transactionList));
        return "history";
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Principal principal){
        Client client = clientService.findByUsername(principal.getName());
        model.addAttribute("client", clientMapper.toDtoResponse(client  ));
        return "profile";
    }

    @GetMapping("/card/{card}")
    public String cardPage(Model model, Principal principal,
                           @PathVariable CardDtoRequest card){
        Card cardOrigin = cardService.findByCardNumber(card.getCardNumber());
        List<Transaction> transactions = transactionService.findByCardId(cardOrigin.getId());

        model.addAttribute("transactions", transactionMapper.toDtoList(transactions));
        model.addAttribute("card", card);
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
    public String loginClient(@ModelAttribute("client") ClientDtoRequest clientDto){
        Client existingClient = clientMapper.fromDtoRequest(clientDto);
        if (clientService.findByUsername(existingClient.getUsername()) == null){
            return "login";
        }
        return "redirect:/account";
    }
}

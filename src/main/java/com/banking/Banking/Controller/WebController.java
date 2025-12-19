package com.banking.Banking.Controller;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Entity.Client;
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
import org.springframework.web.bind.annotation.PostMapping;

import java.nio.Buffer;

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

    @GetMapping("/account")
    public String account(Model model){
        return "account";
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

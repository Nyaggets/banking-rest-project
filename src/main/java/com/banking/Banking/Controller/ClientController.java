package com.banking.Banking.Controller;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.Service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/clients")
@Validated
public class ClientController {
    private final ClientService clientService;
    private final ClientMapper clientMapper;
    public final TransactionService transactionService;
    public final TransactionMapper transactionMapper;
    @Autowired
    public ClientController(ClientService clientService, ClientMapper clientMapper,
                            TransactionService transactionService, TransactionMapper transactionMapper) {
        this.clientService = clientService;
        this.clientMapper = clientMapper;
        this.transactionService = transactionService;
        this.transactionMapper = transactionMapper;
    }

    @GetMapping
    public ResponseEntity<List<ClientDtoResponse>> findAll(){
        List<ClientDtoResponse> clientsDto = clientMapper.toListDtoResponse(clientService.findAll());
        return ResponseEntity.ok(clientsDto);
    }

    @GetMapping("search/{id}")
    public ResponseEntity<ClientDtoResponse> findById(@PathVariable("id") Long id){
        Client client = clientService.findById(id);
        if (client == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clientMapper.toDtoResponse(client));
    }

    @PostMapping("/login/{phone}")
    public ResponseEntity<ClientDtoResponse> loginByPhone(@PathVariable("phone") String phone){
        Client client = clientService.findByPhone(phone);
        if (client == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(clientMapper.toDtoResponse(client));
    }

    @PostMapping("/register")
    public ResponseEntity<ClientDtoResponse> singUpClient(@RequestBody ClientDtoRequest clientDtoRequest){
        Client newClient = clientMapper.fromDtoRequest(clientDtoRequest);
        if (clientService.findByPhone(newClient.getPhone()) != null){
            return ResponseEntity.badRequest().build();
        }
        if (!clientService.createClient(newClient)){
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(clientMapper.toDtoResponse(newClient), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<?> deleteClient(@PathVariable Long id){
        if (!clientService.deleteClient(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}

package com.banking.Banking.Controller;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;
    @Autowired
    private ClientMapper mapper;

    @GetMapping
    public ResponseEntity<List<ClientDtoResponse>> findAll(){
        List<ClientDtoResponse> clientsDto = mapper.toListDtoResponse(clientService.findAll());
        return ResponseEntity.ok(clientsDto);
    }

    @GetMapping("search-id/{id}")
    public ResponseEntity<ClientDtoResponse> findById(@PathVariable("id") Long id){
        Client client = clientService.findById(id);
        if (client == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(client));
    }

    @GetMapping("search-phone/{phone}")
    public ResponseEntity<ClientDtoResponse> findByPhone(@PathVariable("phone") String phone){
        Client client = clientService.findByPhone(phone);
        if (client == null){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toDtoResponse(client));
    }

    @PostMapping("/create")
    public ResponseEntity<ClientDtoResponse> createClient(@RequestBody ClientDtoRequest clientDtoRequest){
        Client newClient = mapper.fromDtoRequest(clientDtoRequest);
        if (clientService.findByPhone(newClient.getPhone()) != null){
            return ResponseEntity.badRequest().build();
        }
        if (!clientService.createClient(newClient)){
            return ResponseEntity.badRequest().build();
        }
        return new ResponseEntity<>(mapper.toDtoResponse(newClient), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<?> deleteClient(@PathVariable Long id){
        if (!clientService.deleteClient(id)){
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}

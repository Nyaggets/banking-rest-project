package com.banking.Banking.Service;

import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService {
    @Autowired
    private ClientRepository repository;

    public boolean createClient(Client client) {
        if (repository.findByPhone(client.getName()).isPresent()) {
            return false;
        }
        repository.save(client);
        return true;
    }

    public List<Client> findAll(){
        return repository.findAll();
    }

    public Client findById(Long id){
        return repository.findById(id).orElse(null);
    }

    public Client findByPhone(String phone){
        return repository.findByPhone(phone).orElse(null);
    }

    public boolean deleteClient(Long id){
        if (repository.findById(id).orElse(null) == null){
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}

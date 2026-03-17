package com.banking.Banking.Service;

import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientService implements UserDetailsService {
    @Autowired
    private ClientRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean createClient(Client client) {
        if (repository.findByPhone(client.getPhone()).isPresent()) {
            return false;
        }
        client.setAuthority("USER");
        client.setPassword(passwordEncoder.encode(client.getPassword()));
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

    public Client findByUsername(String username){
        return repository.findByUsername(username);
    }

    public boolean deleteClient(Long id){
        if (repository.findById(id).orElse(null) == null){
            return false;
        }
        repository.deleteById(id);
        return true;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findByUsername(username);
    }
}

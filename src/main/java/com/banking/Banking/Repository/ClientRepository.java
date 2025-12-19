package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPhone(String phone);
    Client findByUsername(String username);
}

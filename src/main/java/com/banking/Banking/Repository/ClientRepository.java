package com.banking.Banking.Repository;

import com.banking.Banking.Entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByPhone(String phone);
    Optional<Client> findByLogin(String login);
    boolean existsByPhoneAndIdNot(String phone, Long clientId);
    boolean existsByLoginAndIdNot(String login, Long clientId);
}

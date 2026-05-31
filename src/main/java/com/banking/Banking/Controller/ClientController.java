package com.banking.Banking.Controller;

import com.banking.Banking.Dto.ClientDtoResponse;
import com.banking.Banking.Dto.PassportDto;
import com.banking.Banking.Dto.UpdatePasswordDto;
import com.banking.Banking.Dto.UpdateSafeDataDto;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Mapper.TransactionMapper;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.Service.TransactionService;
import com.banking.Banking.validation.RequestLimitException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    private ResponseEntity<Map<Object, String>> validateBindingResult(BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(
                    result.getFieldErrors().stream().collect(
                            Collectors.toMap(
                                    error ->error.getField(),
                                    FieldError::getDefaultMessage
                            ))
            );
        }
        return null;
    }

    private void updateAuthentification(Client client, Authentication auth) {
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                client.getLogin(),
                auth.getCredentials(),
                auth.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);
    }

    @PatchMapping("/{clientId}/profile")
    public ResponseEntity<?> updateClient(Authentication auth, @PathVariable Long clientId,
                                          @RequestBody @Validated UpdateSafeDataDto dtoParams, BindingResult result) {
        ResponseEntity<Map<Object, String>> errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            return ResponseEntity.badRequest().body(errorResponse);

        Client client = clientService.updateClient(clientId, dtoParams);
        updateAuthentification(client, auth);
        return ResponseEntity.ok(clientMapper.toDtoResponse(client));
    }

    @PatchMapping("/{clientId}/password")
    public ResponseEntity<?> updateClient(Authentication auth,  @PathVariable Long clientId,
                                          @RequestBody @Validated UpdatePasswordDto dtoParams, BindingResult result) {
        ResponseEntity<Map<Object, String>> errorResponse = validateBindingResult(result);
        if (errorResponse != null)
            return ResponseEntity.badRequest().body(errorResponse);

        Client client = clientService.updateClient(clientId, dtoParams);
        updateAuthentification(client, auth);
        return ResponseEntity.ok(clientMapper.toDtoResponse(client));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/reveal-passport")
    public ResponseEntity<PassportDto> revealPassport(Authentication auth, @RequestBody Map<String, String> requestBody)
            throws RequestLimitException {
        Client client = clientService.findByLogin(auth.getName());
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(clientService.revealPassport(client.getId(), requestBody.get("password")));
    }
}

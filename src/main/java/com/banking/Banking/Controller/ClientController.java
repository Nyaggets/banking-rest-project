package com.banking.Banking.Controller;

import com.banking.Banking.Dto.PassportDto;
import com.banking.Banking.Dto.UpdatePasswordDto;
import com.banking.Banking.Dto.UpdateSafeDataDto;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.SessionUser;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Service.ClientService;
import com.banking.Banking.validation.RequestLimitException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@PreAuthorize("isAuthenticated()")
@RestController
@RequestMapping("/api/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;
    @Autowired
    private ClientMapper clientMapper;

    /**
     * Получение данных текущего пользователя
     */
    @GetMapping("/me")
    public ResponseEntity<SessionUser> getCurrentUser(Authentication auth){
        if (auth == null || auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated())
            throw new BadCredentialsException("Пользователь не найден");
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(client);
    }

    /**
     * Получение данных профиля текущего пользователя
     */
    @PatchMapping("/profile")
    public ResponseEntity<?> updateClient(Authentication auth, @RequestBody @Valid UpdateSafeDataDto dtoParams) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Client updatedClient = clientService.updateClient(client.getId(), dtoParams);
        return ResponseEntity.ok(clientMapper.toDtoResponse(updatedClient));
    }

    /**
     * Изменение пароля текущего пользователя
     */
    @PatchMapping("/password")
    public ResponseEntity<?> updateClient(Authentication auth, @RequestBody @Valid UpdatePasswordDto dtoParams) {
        SessionUser client = (SessionUser) auth.getPrincipal();

        Client updatedClient = clientService.updateClient(client.getId(), dtoParams);
        return ResponseEntity.ok(clientMapper.toDtoResponse(updatedClient));
    }

    /**
     * Получение данных паспорта текущего пользователя
     */
    @PostMapping("/reveal-passport")
    public ResponseEntity<PassportDto> revealPassport(Authentication auth, @RequestBody Map<String, String> requestBody) throws RequestLimitException {
        SessionUser client = (SessionUser) auth.getPrincipal();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(clientService.revealPassport(client.getId(), requestBody.get("password")));
    }
}

package com.banking.Banking.Service;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.PassportDto;
import com.banking.Banking.Dto.UpdatePasswordDto;
import com.banking.Banking.Dto.UpdateSafeDataDto;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Entity.SessionUser;
import com.banking.Banking.Repository.ClientRepository;
import com.banking.Banking.validation.CustomException;
import com.banking.Banking.validation.RequestLimitException;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ClientService implements UserDetailsService {
    private ClientRepository repository;
    private PasswordEncoder passwordEncoder;
    private EncodeService encodeService;
    private VerifyIdentityService attemptsCount;
    private PhoneService phoneService;

    public ClientService(ClientRepository repository,PasswordEncoder passwordEncoder, EncodeService encodeService,
                         VerifyIdentityService attemptsCount, PhoneService phoneService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.encodeService = encodeService;
        this.attemptsCount = attemptsCount;
        this.phoneService = phoneService;
    }

    public boolean createClient(ClientDtoRequest dto) {
        if (repository.findByPhone(dto.getPhone()).isPresent())
            throw new CustomException("DUPLICATE", Map.of("phone", "Пользователь с такими номером телефона уже существует"));
        if (repository.findByLogin(dto.getLogin()).isPresent())
            throw new CustomException("DUPLICATE", Map.of("login", "Пользователь с такими логином уже существует"));

        Client client = Client.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .patronymic(dto.getPatronymic())
                .phone(phoneService.normalizePhone(dto.getPhone()))
                .authority("USER")
                .login(dto.getLogin())
                .password(passwordEncoder.encode(dto.getPassword()))
                .passportSeries(encodeService.encodeString(dto.getPassportSeries()))
                .passportNumber(encodeService.encodeString(dto.getPassportNumber()))
                .passportIssuedBy(encodeService.encodeString(dto.getPassportIssuedBy()))
                .passportDepartmentCode(encodeService.encodeString(dto.getPassportDepartmentCode()))
                .passportIssueDate(String.valueOf(dto.getPassportIssueDate()))
                .build();
        repository.save(client);
        return true;
    }

    public Client updateClient(Long clientId, UpdateSafeDataDto dto) {
        Client client = findByIdOrThrow(clientId);
        Map<String, String> errors = new HashMap<>();
        if (dto.getPhone() != null) {
            if (!repository.existsByPhoneAndIdNot(dto.getPhone(), clientId))
                client.setPhone(phoneService.normalizePhone(dto.getPhone()));
            else
                errors.put("phone", "Пользователь с таким телефоном уже существует");
        }
        if (dto.getLogin() != null) {
            if (!repository.existsByLoginAndIdNot(dto.getLogin(), clientId))
                client.setLogin(dto.getLogin());
            else
                errors.put("login", "Логин занят");
        }
        if (!errors.isEmpty())
            throw new CustomException("VALIDATION ERROR", errors);

        return repository.save(client);
    }

    public Client updateClient(Long clientId, UpdatePasswordDto dto) {
        Client client = findByIdOrThrow(clientId);
        Map<String, String> errors = new HashMap<>();
        if (!passwordEncoder.matches(dto.getOldPassword(), client.getPassword()))
            errors.put("oldPassword", "Неверный текущий пароль");
        if (!dto.getNewPassword().equals(dto.getPasswordConf()))
            errors.put("passwordConf", "Пароли не совпадают");
        if (passwordEncoder.matches(dto.getNewPassword(), client.getPassword()))
            errors.put("newPassword", "Новый пароль совпадает с текущим");

        if (!errors.isEmpty())
            throw new CustomException("VALIDATION ERROR", errors);

        client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        return repository.save(client);
    }

    /**
     * Метод для получения полных данных паспорта с учетом количества попыток подтверждения личности
     */
    public PassportDto revealPassport(Long clientId, String password) throws AccessDeniedException, RequestLimitException {
        attemptsCount.throwIfPasswordAttemptLimit(clientId, checkPassword(password, clientId));
        Client client = findByIdOrThrow(clientId);
        return PassportDto.builder()
                .series(encodeService.decodeString(client.getPassportSeries()))
                .number(encodeService.decodeString(client.getPassportNumber()))
                .fullName(client.getFullName())
                .issuedBy(encodeService.decodeString(client.getPassportIssuedBy()))
                .departmentCode(encodeService.decodeString(client.getPassportDepartmentCode()))
                .issueDate(LocalDate.parse(encodeService.decodeString(client.getPassportIssueDate()), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .build();
    }

    public Client findByPhone(String phone){
        String normalizedPhone = phoneService.normalizePhone(phone);
        return repository.findByPhone(normalizedPhone).orElse(null);
    }

    public Client findByLoginOrThrow(String login) {
        return repository.findByLogin(login)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
    }

    public Client findByIdOrThrow(Long id){
        return repository.findById(id)
                .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
    }

    public boolean checkPassword(String password, Long clientId) {
        Client client = findByIdOrThrow(clientId);
        return passwordEncoder.matches(password, client.getPassword());
    }

    @Override
    public UserDetails loadUserByUsername(String userIdentifier) {
        String login = userIdentifier;
        if (userIdentifier.matches("^(\\+?7|8)\\d{10}$")) {
            Optional<Client> client = repository.findByPhone(userIdentifier);
            login = client.map(Client::getLogin).orElse(null);
        }
        Client client = findByLoginOrThrow(login);
        return new SessionUser(client);
    }
}

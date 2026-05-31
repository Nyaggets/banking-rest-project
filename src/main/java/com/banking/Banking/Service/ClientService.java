package com.banking.Banking.Service;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.PassportDto;
import com.banking.Banking.Dto.UpdatePasswordDto;
import com.banking.Banking.Dto.UpdateSafeDataDto;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Mapper.ClientMapper;
import com.banking.Banking.Repository.CardRepository;
import com.banking.Banking.Repository.ClientRepository;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ClientService implements UserDetailsService {
    private CardRepository cardRepository;
    private ClientRepository repository;
    private ClientMapper mapper;
    private PasswordEncoder passwordEncoder;
    private EncodeService encodeService;
    private VerifyIdentityService attemptsCount;

    public ClientService(CardRepository cardRepository, ClientRepository repository,
                         ClientMapper mapper, PasswordEncoder passwordEncoder, EncodeService encodeService, VerifyIdentityService attemptsCount) {
        this.cardRepository = cardRepository;
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.encodeService = encodeService;
        this.attemptsCount = attemptsCount;
    }

    public String normalizePhone(String phone) {
        if (phone == null || phone.isBlank())
            return null;

        String phoneDigits = phone.replaceAll("[^\\d]", "");
        if (phoneDigits.length() == 11 && phoneDigits.startsWith("7"))
            phoneDigits = "8" + phoneDigits.substring(1);
        if (phoneDigits.length() == 10)
            return "8" + phoneDigits;

        return phoneDigits;
    }

    public boolean createClient(ClientDtoRequest dto) {
        if (repository.findByPhone(dto.getPhone()).isPresent())
            throw new BadCredentialsException("Пользователь с такими номером телефона уже существует");
        if (repository.findByLogin(dto.getLogin()).isPresent())
            throw new BadCredentialsException("Пользователь с такими логином уже существует");
        if (repository.findByPhone(dto.getPhone()).isPresent())
            throw new BadCredentialsException("Пользователь с такими номером телефона уже существует");
        Client client = Client.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .patronymic(dto.getPatronymic())
                .phone(normalizePhone(dto.getPhone()))
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
//        var clientT = findByLogin("Maria");
//        clientT.setPassportSeries(encodeService.encodeString("7825"));
//        clientT.setPassportNumber(encodeService.encodeString("123321"));
//        clientT.setPassportIssuedBy(encodeService.encodeString("УМВД России по Ярославской области"));
//        clientT.setPassportIssueDate(encodeService.encodeString("2020.05.07"));
//        clientT.setPassportDepartmentCode(encodeService.encodeString("760-007"));
//
//        var clientT2 = findByLogin("Anya");
//        clientT2.setPassportSeries(encodeService.encodeString("7825"));
//        clientT2.setPassportNumber(encodeService.encodeString("789987"));
//        clientT2.setPassportIssuedBy(encodeService.encodeString("УМВД России по Ярославской области"));
//        clientT2.setPassportIssueDate(encodeService.encodeString("2021.10.13"));
//        clientT2.setPassportDepartmentCode(encodeService.encodeString("760-007"));

        Client client = findByIdOrThrow(clientId);
        if (dto.getPhone() != null) {
            if (repository.existsByPhoneAndIdNot(dto.getPhone(), clientId))
                throw new IllegalArgumentException("Пользователь с таким телефоном уже существует");
            client.setPhone(normalizePhone(dto.getPhone()));
        }
        if (dto.getLogin() != null) {
            if (repository.existsByLoginAndIdNot(dto.getLogin(), clientId))
                throw new IllegalArgumentException("Логин занят");
            client.setLogin(dto.getLogin());
        }
        return repository.save(client);
    }

    public Client updateClient(Long clientId, UpdatePasswordDto dto) {
        Client client = findByIdOrThrow(clientId);
        client.setPassword(passwordEncoder.encode("maria"));
        if (!dto.getNewPassword().equals(dto.getPasswordConf()))
            throw new IllegalArgumentException("Пароли не совпадают");
        if (!passwordEncoder.matches(dto.getOldPassword(), client.getPassword()))
            throw new IllegalArgumentException("Неверный текущий пароль");
        if (passwordEncoder.matches(dto.getNewPassword(), client.getPassword()))
            throw new IllegalArgumentException("Новый пароль совпадает с текущим");

        client.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        return repository.save(client);
    }

    public PassportDto revealPassport(Long clientId, String password) throws AccessDeniedException, RequestLimitException {
        attemptsCount.throwIfAttemptLimit(clientId, password, checkPassword(password, clientId));
        Client client = findByIdOrThrow(clientId);
        return PassportDto.builder()
                .series(encodeService.decodeString(client.getPassportSeries()))
                .number(encodeService.decodeString(client.getPassportNumber()))
                .issuedBy(encodeService.decodeString(client.getPassportIssuedBy()))
                .departmentCode(encodeService.decodeString(client.getPassportDepartmentCode()))
                .issueDate(LocalDate.parse(encodeService.decodeString(client.getPassportIssueDate()), DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .build();
    }

    public List<Client> findAll(){
        return repository.findAll();
    }

    public Client findByPhone(String phone){
        return repository.findByPhone(phone).orElse(null);
    }

    public Client findByLogin(String username){
        return repository.findByLogin(username).orElse(null);
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
        if (userIdentifier.matches("^(\\+7|8)\\d{10}$")) {
            Optional<Client> client = repository.findByPhone(userIdentifier);
            login = client.map(Client::getLogin).orElse(null);
        }
        return repository.findByLogin(login)
            .orElseThrow(() -> new BadCredentialsException("Пользователь не найден"));
    }
}

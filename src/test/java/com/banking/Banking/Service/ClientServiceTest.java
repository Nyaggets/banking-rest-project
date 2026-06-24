package com.banking.Banking.Service;

import com.banking.Banking.Dto.ClientDtoRequest;
import com.banking.Banking.Dto.PassportDto;
import com.banking.Banking.Dto.UpdatePasswordDto;
import com.banking.Banking.Dto.UpdateDataDto;
import com.banking.Banking.Entity.Client;
import com.banking.Banking.Repository.ClientRepository;
import com.banking.Banking.validation.CustomException;
import com.banking.Banking.validation.RequestLimitException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EncodeService encodeService;
    @Mock
    private VerifyIdentityService attemptsCount;
    @Mock
    private PhoneService phoneService;
    @InjectMocks
    private ClientService clientService;

    private Client existingClient;
    private ClientDtoRequest createDto;
    private UpdateDataDto updateDataDto;
    private UpdatePasswordDto updatePasswordDto;

    @BeforeEach
    void setUp() {
        existingClient = Client.builder()
                .id(1L)
                .login("existingUser")
                .password("passwordHash")
                .phone("+79001234567")
                .passportSeries("encSeries")
                .passportNumber("encNum")
                .passportIssuedBy("encIssued")
                .passportDepartmentCode("encDepartment")
                .passportIssueDate("2026.01.01")
                .name("Name")
                .surname("Surname")
                .patronymic("Patronymic")
                .authority("USER")
                .build();

        createDto = ClientDtoRequest.builder()
                .login("newUser")
                .phone("+79009998877")
                .password("rawPassword")
                .passportSeries("1234")
                .passportNumber("567890")
                .passportIssuedBy("MVD")
                .passportDepartmentCode("001-002")
                .passportIssueDate(LocalDate.of(2026, 1, 1))
                .name("Ivan")
                .surname("Ivanov")
                .patronymic("Ivanovich")
                .build();

        updateDataDto = UpdateDataDto.builder()
                .phone("+79001112233")
                .login("updatedLogin")
                .build();

        updatePasswordDto = UpdatePasswordDto.builder()
                .oldPassword("oldPassword")
                .newPassword("updatedPassword")
                .passwordConf("updatedPassword")
                .build();
    }
    
    @Test
    void createClient_Success() {
        when(phoneService.normalizePhone(anyString())).thenReturn("+7 900 999 88 77");
        when(passwordEncoder.encode(anyString())).thenReturn("passwordHash");
        when(encodeService.encodeString(anyString())).thenReturn("encodedString");
        when(clientRepository.findByPhone(anyString())).thenReturn(Optional.empty());
        when(clientRepository.findByLogin(anyString())).thenReturn(Optional.empty());
        when(clientRepository.save(any())).thenReturn(existingClient);

        boolean result = clientService.createClient(createDto) != null;

        assertTrue(result);
        verify(clientRepository, times(1)).save(any());
    }

    @Test
    void createClient_PhoneAlreadyExists() {
        when(clientRepository.findByPhone(anyString())).thenReturn(Optional.of(existingClient));

        assertThrows(CustomException.class, () -> clientService.createClient(createDto));
    }

    @Test
    void createClient_LoginAlreadyExists() {
        when(clientRepository.findByLogin(anyString())).thenReturn(Optional.of(existingClient));

        assertThrows(CustomException.class, () -> clientService.createClient(createDto));
    }
    
    @Test
    void updateClientSafe_Success() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByPhoneAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(phoneService.normalizePhone(anyString())).thenReturn("+79001112233");
        when(clientRepository.existsByLoginAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clientRepository.save(any())).thenReturn(existingClient);

        Client updated = clientService.updateClient(1L, updateDataDto);

        assertEquals("+79001112233", updated.getPhone());
        assertEquals("updatedLogin", updated.getLogin());
    }

    @Test
    void updateClientSafe_LoginTaken() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByPhoneAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(clientRepository.existsByLoginAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThrows(CustomException.class, () -> clientService.updateClient(1L, updateDataDto));
    }

    @Test
    void updateClientSafe_PhoneTaken() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(clientRepository.existsByPhoneAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThrows(CustomException.class, () -> clientService.updateClient(1L, updateDataDto));
    }
    
    @Test
    void updateClientPassword_Success() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(passwordEncoder.matches("oldPassword", existingClient.getPassword())).thenReturn(true);
        when(passwordEncoder.matches("updatedPassword", existingClient.getPassword())).thenReturn(false);
        when(passwordEncoder.encode("updatedPassword")).thenReturn("updatedPasswordHash");
        when(clientRepository.save(any())).thenReturn(existingClient);

        Client updated = clientService.updateClient(1L, updatePasswordDto);

        assertEquals("updatedPasswordHash", updated.getPassword());
    }

    @Test
    void updateClientPassword_WrongOldPassword() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(passwordEncoder.matches("oldPassword", existingClient.getPassword())).thenReturn(false);

        assertThrows(CustomException.class, () -> clientService.updateClient(1L, updatePasswordDto));
    }

    @Test
    void updateClientPassword_WrongPasswordConfirm() {
        updatePasswordDto.setPasswordConf("wrongPassword");
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));

        assertThrows(CustomException.class, () -> clientService.updateClient(1L, updatePasswordDto));
    }
    
    @Test
    void revealPassport_Success() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(passwordEncoder.matches("correctPassword", existingClient.getPassword())).thenReturn(true);
        doNothing().when(attemptsCount).throwIfPasswordAttemptLimit(anyLong(), eq(true));

        when(encodeService.decodeString("encSeries")).thenReturn("1234");
        when(encodeService.decodeString("encNum")).thenReturn("567890");
        when(encodeService.decodeString("encIssued")).thenReturn("FMS");
        when(encodeService.decodeString("encDepartment")).thenReturn("001-002");
        when(encodeService.decodeString("2026.01.01")).thenReturn("2026.01.01");

        PassportDto passport = clientService.revealPassport(1L, "correctPassword");

        assertEquals("1234", passport.getSeries());
        assertEquals("567890", passport.getNumber());
        assertEquals("Surname Name Patronymic", passport.getFullName());
    }

    @Test
    void revealPassport_InvalidPassword() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(existingClient));
        when(passwordEncoder.matches("wrongPass", existingClient.getPassword())).thenReturn(false);
        doThrow(new RequestLimitException("Превышен лимит", Instant.now())).when(attemptsCount)
                .throwIfPasswordAttemptLimit(anyLong(), eq(false));

        assertThrows(RequestLimitException.class, () -> clientService.revealPassport(1L, "wrongPass"));
    }
    
    @Test
    void loadUserByUsername_Success() {
        when(clientRepository.findByLogin("existingUser")).thenReturn(Optional.of(existingClient));

        UserDetails details = clientService.loadUserByUsername("existingUser");

        assertEquals("existingUser", details.getUsername());
    }

    @Test
    void loadUserByUsername_UserNotFound() {
        when(clientRepository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class, () -> clientService.loadUserByUsername("unknown"));
    }
}
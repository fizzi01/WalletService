package it.unisalento.pasproject.walletservice.services;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.UserDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.UsersDataHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static it.unisalento.pasproject.walletservice.security.WalletConstants.UTENTE_INITIAL_BALANCE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {UsersDataHandler.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class UsersDataHandlerTests {

    @MockBean
    private WalletRepository walletRepository;

    @InjectMocks
    private UsersDataHandler usersDataHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletRepository = mock(WalletRepository.class);
        usersDataHandler = new UsersDataHandler(walletRepository);

        given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void receiveMessage_adminUserDetected() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("admin@example.com");
        userDTO.setRole("admin");

        usersDataHandler.receiveMessage(userDTO);

        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void receiveMessage_userAlreadyExists() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("user@example.com");
        userDTO.setRole("utente");

        Wallet existingWallet = new Wallet();
        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        usersDataHandler.receiveMessage(userDTO);

        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void receiveMessage_createsNewUserWithInitialBalance() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("newuser@example.com");
        userDTO.setRole("utente");

        when(walletRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        usersDataHandler.receiveMessage(userDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();

        assertEquals("newuser@example.com", savedWallet.getEmail());
        assertEquals(UTENTE_INITIAL_BALANCE, savedWallet.getBalance());
        assertTrue(savedWallet.getIsEnable());
    }

    @Test
    void receiveMessage_createsNewUserWithoutInitialBalance() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("newuser@example.com");
        userDTO.setRole("anotherRole");

        when(walletRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());

        usersDataHandler.receiveMessage(userDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();

        assertEquals("newuser@example.com", savedWallet.getEmail());
        assertEquals(0.0, savedWallet.getBalance());
        assertTrue(savedWallet.getIsEnable());
    }

    @Test
    void receiveMessage_exceptionHandling() {
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail("newuser@example.com");
        userDTO.setRole("utente");

        when(walletRepository.findByEmail("newuser@example.com")).thenThrow(new RuntimeException("Database error"));

        usersDataHandler.receiveMessage(userDTO);

        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
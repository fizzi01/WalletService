package it.unisalento.pasproject.walletservice.services;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.GeneralDataDTO;
import it.unisalento.pasproject.walletservice.dto.GeneralRequestDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.GeneralDataHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static it.unisalento.pasproject.walletservice.security.WalletConstants.UTENTE_INITIAL_BALANCE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {GeneralDataHandler.class})
class GeneralDataHandlerTests {

    @MockBean
    private WalletRepository walletRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private GeneralDataHandler generalDataHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletRepository = mock(WalletRepository.class);
        generalDataHandler = new GeneralDataHandler(walletRepository);

        given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
    }


    @Test
    void receiveMessage_createsNewWalletIfNotExists() {
        GeneralDataDTO dataDTO = new GeneralDataDTO();
        dataDTO.setId("new@example.com");

        when(walletRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        generalDataHandler.receiveMessage(dataDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(1)).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();

        assertEquals("new@example.com", savedWallet.getEmail());

    }


    @Test
    void receiveRequest_disablesWalletWhenRequested() {
        Wallet existingWallet = new Wallet();
        existingWallet.setBalance(100.0);
        existingWallet.setEmail("user@example.com");
        existingWallet.setId("1");
        existingWallet.setIsEnable(true);
        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        GeneralRequestDTO requestDTO = new GeneralRequestDTO();
        requestDTO.setEmail("user@example.com");
        requestDTO.setRequestType(GeneralRequestDTO.RequestType.DISABLE);

        generalDataHandler.receiveRequest(requestDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertFalse(walletCaptor.getValue().getIsEnable());
    }

    @Test
    void receiveRequest_enablesWalletWhenRequested() {
        Wallet existingWallet = new Wallet();
        existingWallet.setBalance(100.0);
        existingWallet.setEmail("user@example.com");
        existingWallet.setId("1");
        existingWallet.setIsEnable(false);

        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        GeneralRequestDTO requestDTO = new GeneralRequestDTO();
        requestDTO.setEmail("user@example.com");
        requestDTO.setRequestType(GeneralRequestDTO.RequestType.ENABLE);

        generalDataHandler.receiveRequest(requestDTO);

        // Verifica che il metodo save sia stato chiamato con il wallet abilitato
        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());

        Wallet savedWallet = walletCaptor.getValue();

        // Assegnazioni per verificare lo stato del wallet
        assertTrue(savedWallet.getIsEnable(), "Il wallet dovrebbe essere abilitato.");

    }

    @Test
    void receiveRequest_increasesBalanceWhenDepositRequested() {
        Wallet existingWallet = new Wallet();
        existingWallet.setBalance(100.0);
        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        GeneralRequestDTO requestDTO = new GeneralRequestDTO();
        requestDTO.setEmail("user@example.com");
        requestDTO.setRequestType(GeneralRequestDTO.RequestType.DEPOSIT);
        requestDTO.setAmount(50.0);
        generalDataHandler.receiveRequest(requestDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(150.0, walletCaptor.getValue().getBalance());
    }

    @Test
    void receiveRequest_resetsBalanceWhenRefillRequested() {
        Wallet existingWallet = new Wallet();
        existingWallet.setBalance(100.0);
        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        GeneralRequestDTO requestDTO = new GeneralRequestDTO();
        requestDTO.setEmail("user@example.com" );
        requestDTO.setRequestType(GeneralRequestDTO.RequestType.REFILL);
        requestDTO.setAmount(0.0);

        generalDataHandler.receiveRequest(requestDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(UTENTE_INITIAL_BALANCE, walletCaptor.getValue().getBalance());

    }

    @Test
    void receiveRequest_setsBalanceToZeroWhenSubtractRequestedAndResultIsNegative() {
        Wallet existingWallet = new Wallet();
        existingWallet.setBalance(50.0);
        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        GeneralRequestDTO requestDTO = new GeneralRequestDTO();
        requestDTO.setEmail("user@example.com" );
        requestDTO.setRequestType(GeneralRequestDTO.RequestType.SUBTRACT);
        requestDTO.setAmount(100.0);

        generalDataHandler.receiveRequest(requestDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(0.0, walletCaptor.getValue().getBalance());
    }

    @Test
    void receiveRequest_subtractsFromBalanceWhenSubtractRequested() {
        Wallet existingWallet = new Wallet();
        existingWallet.setBalance(100.0);
        when(walletRepository.findByEmail("user@example.com")).thenReturn(Optional.of(existingWallet));

        GeneralRequestDTO requestDTO = new GeneralRequestDTO();
        requestDTO.setEmail("user@example.com" );
        requestDTO.setRequestType(GeneralRequestDTO.RequestType.SUBTRACT);
        requestDTO.setAmount(50.0);

        generalDataHandler.receiveRequest(requestDTO);

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assertEquals(50.0, walletCaptor.getValue().getBalance());
    }
}
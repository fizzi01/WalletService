package it.unisalento.pasproject.walletservice.services;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.MessageDTO;
import it.unisalento.pasproject.walletservice.dto.RequestTransactionDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.WalletMessageHandler;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {WalletMessageHandler.class})
@MockitoSettings(strictness = Strictness.LENIENT)
class WalletMessageHandlerTests {

    @MockBean
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletMessageHandler walletMessageHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletRepository = mock(WalletRepository.class);
        walletMessageHandler = new WalletMessageHandler(walletRepository);

        given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void receiveTransaction_senderNotFound() {
        RequestTransactionDTO request = new RequestTransactionDTO();
        request.setSenderEmail("sender@example.com");
        request.setReceiverEmail("receiver@example.com");
        request.setAmount(100.0);
        request.setId("tx1");

        when(walletRepository.findByEmail("sender@example.com")).thenReturn(Optional.empty());

        MessageDTO response = walletMessageHandler.receiveTransaction(request);

        assertEquals(400, response.getCode());
        assertEquals("tx1", response.getResponse());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void receiveTransaction_senderNotEnoughMoney() {
        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(50.0);
        senderWallet.setIsEnable(true);

        when(walletRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(senderWallet));

        RequestTransactionDTO request = new RequestTransactionDTO();
        request.setSenderEmail("sender@example.com");
        request.setReceiverEmail("receiver@example.com");
        request.setAmount(100.0);
        request.setId("tx1");

        MessageDTO response = walletMessageHandler.receiveTransaction(request);

        assertEquals(400, response.getCode());
        assertEquals("tx1", response.getResponse());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void receiveTransaction_receiverNotFound() {
        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(150.0);
        senderWallet.setIsEnable(true);

        when(walletRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmail("receiver@example.com")).thenReturn(Optional.empty());

        RequestTransactionDTO request = new RequestTransactionDTO();
        request.setSenderEmail("sender@example.com");
        request.setReceiverEmail("receiver@example.com");
        request.setAmount(100.0);
        request.setId("tx1");

        MessageDTO response = walletMessageHandler.receiveTransaction(request);

        assertEquals(400, response.getCode());
        assertEquals("tx1", response.getResponse());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void receiveTransaction_receiverNotEnabled() {
        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(150.0);
        senderWallet.setIsEnable(true);

        Wallet receiverWallet = new Wallet();
        receiverWallet.setIsEnable(false);

        when(walletRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiverWallet));

        RequestTransactionDTO request = new RequestTransactionDTO();
        request.setSenderEmail("sender@example.com");
        request.setReceiverEmail("receiver@example.com");
        request.setAmount(100.0);
        request.setId("tx1");

        MessageDTO response = walletMessageHandler.receiveTransaction(request);

        assertEquals(400, response.getCode());
        assertEquals("tx1", response.getResponse());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void receiveTransaction_successfulTransaction() {
        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(150.0);
        senderWallet.setIsEnable(true);

        Wallet receiverWallet = new Wallet();
        receiverWallet.setBalance(50.0);
        receiverWallet.setIsEnable(true);

        when(walletRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiverWallet));

        RequestTransactionDTO request = new RequestTransactionDTO();
        request.setSenderEmail("sender@example.com");
        request.setReceiverEmail("receiver@example.com");
        request.setAmount(100.0);
        request.setId("tx1");

        MessageDTO response = walletMessageHandler.receiveTransaction(request);

        assertEquals(200, response.getCode());
        assertEquals("tx1", response.getResponse());

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository, times(2)).save(walletCaptor.capture());

        Wallet updatedSenderWallet = walletCaptor.getAllValues().get(0);
        Wallet updatedReceiverWallet = walletCaptor.getAllValues().get(1);

        assertEquals(50.0, updatedSenderWallet.getBalance());
        assertEquals(150.0, updatedReceiverWallet.getBalance());
    }

    @Test
    void receiveTransaction_transactionFailsOnSave() {
        Wallet senderWallet = new Wallet();
        senderWallet.setBalance(150.0);
        senderWallet.setIsEnable(true);

        Wallet receiverWallet = new Wallet();
        receiverWallet.setBalance(50.0);
        receiverWallet.setIsEnable(true);

        when(walletRepository.findByEmail("sender@example.com")).thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByEmail("receiver@example.com")).thenReturn(Optional.of(receiverWallet));
        doThrow(new RuntimeException()).when(walletRepository).save(any(Wallet.class));

        RequestTransactionDTO request = new RequestTransactionDTO();
        request.setSenderEmail("sender@example.com");
        request.setReceiverEmail("receiver@example.com");
        request.setAmount(100.0);
        request.setId("tx1");

        MessageDTO response = walletMessageHandler.receiveTransaction(request);

        assertEquals(400, response.getCode());
        assertEquals("tx1", response.getResponse());
    }
}
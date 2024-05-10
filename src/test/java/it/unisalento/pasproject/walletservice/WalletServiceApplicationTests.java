package it.unisalento.pasproject.walletservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import it.unisalento.pasproject.walletservice.WalletServiceApplication;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WalletServiceApplicationTests {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletServiceApplication walletServiceApplication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnZeroBalance() {
        when(walletRepository.findByEmail("newUser").isPresent()).thenReturn(null);
        double balance = walletRepository.findByEmail("newUser").get().getBalance();
        assertThat(balance).isEqualTo(0.0);
    }

}
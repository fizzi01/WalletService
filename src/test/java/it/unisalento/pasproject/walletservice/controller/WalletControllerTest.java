package it.unisalento.pasproject.walletservice.controller;

import it.unisalento.pasproject.walletservice.TestSecurityConfig;
import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.WalletDTO;
import it.unisalento.pasproject.walletservice.dto.WalletListDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.UserCheckService;
import it.unisalento.pasproject.walletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WalletController.class)
@AutoConfigureMockMvc()
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
class WalletControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private WalletService walletService;

    @MockBean
    private WalletRepository walletRepository;

    @MockBean(answer = Answers.CALLS_REAL_METHODS)
    private UserCheckService userCheckService;

    @InjectMocks
    private WalletController walletController;

    @BeforeEach
    void setUp() {
        given(walletRepository.save(any(Wallet.class))).willAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void addWalletAsAdminCreatesWalletWithSpecifiedBalance() throws Exception {
        when(userCheckService.isAdministrator()).thenReturn(true);
        when(walletRepository.findByEmail(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/wallet/add")
                        .param("email", "user@example.com")
                        .param("balance", "100.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(100.0)));

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assert walletCaptor.getValue().getBalance() == 100.0;
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void addWalletAsUserSetsBalanceToZero() throws Exception {
        when(walletRepository.findByEmail(any())).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/wallet/add")
                        .param("email", "user@example.com")
                        .param("balance", "50.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(0.0)));

        ArgumentCaptor<Wallet> walletCaptor = ArgumentCaptor.forClass(Wallet.class);
        verify(walletRepository).save(walletCaptor.capture());
        assert walletCaptor.getValue().getBalance() == 0.0;
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void addWalletForDifferentUserThrowsException() throws Exception {
        when(userCheckService.isAdministrator()).thenReturn(false);

        mockMvc.perform(post("/api/users/wallet/add")
                        .param("email", "other@example.com")
                        .param("balance", "100.0"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void addWalletWhenWalletExistsReturnsExistingWallet() throws Exception {
        Wallet existingWallet = new Wallet();
        existingWallet.setEmail("user@example.com");
        existingWallet.setBalance(100.0);
        existingWallet.setIsEnable(true);

        when(userCheckService.isAdministrator()).thenReturn(true);
        when(walletRepository.findByEmail(eq("user@example.com"))).thenReturn(Optional.of(existingWallet));

        mockMvc.perform(post("/api/users/wallet/add")
                        .param("email", "user@example.com")
                        .param("balance", "200.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(100.0))); // Expecting the balance to remain as in the existing wallet

        verify(walletRepository).findByEmail("user@example.com");
    }


    @Test
    @WithMockUser(username = "valid@example.com", roles = {"MEMBRO"})
    void getWalletWhenValidEmailReturnWallet() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId("1");
        wallet.setEmail("valid@example.com");
        wallet.setBalance(0.0);
        wallet.setIsEnable(true);
        given(walletRepository.findByEmail("valid@example.com")).willReturn(Optional.of(wallet));

        mockMvc.perform(get("/api/users/wallet/find?email=valid@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("valid@example.com")));
    }

    @Test
    @WithMockUser(username = "current@user.com", roles = {"MEMBRO"})
    void getWalletWhenEmailIsNullShouldUseCurrentSessionUser() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId("1");
        wallet.setEmail("current@user.com");
        wallet.setBalance(0.0);
        wallet.setIsEnable(true);
        given(walletRepository.findByEmail("current@user.com")).willReturn(Optional.of(wallet));

        mockMvc.perform(get("/api/users/wallet/find"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("current@user.com")));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"MEMBRO"})
    void addWalletWhenNotAdminAndDifferentUserEmailShouldReturnForbidden() throws Exception {
        mockMvc.perform(post("/api/users/wallet/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"different@example.com\",\"balance\":100.0}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "valid@example.com", roles = {"MEMBRO"})
    void addWalletWhenNewWalletShouldPersistAndReturnWallet() throws Exception {
        when(walletRepository.findByEmail("valid@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/users/wallet/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"valid@example.com\",\"balance\":100.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("valid@example.com")))
                .andExpect(jsonPath("$.balance", is(0.0)));
    }

    @Test
    @WithMockUser(username = "valid@example.com", roles = {"MEMBRO"})
    void addWalletWhenWalletExistsReturnwallet() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setId("1");
        wallet.setEmail("valid@example.com");
        wallet.setBalance(100.0);
        when(walletRepository.findByEmail("valid@example.com")).thenReturn(Optional.of(wallet));

        mockMvc.perform(post("/api/users/wallet/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"valid@example.com\",\"balance\":5.0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is("valid@example.com")))
                .andExpect(jsonPath("$.balance", is(100.0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWalletsWhenAdminShouldReturnListOfWallets() throws Exception {
        Wallet walletOne = new Wallet();
        walletOne.setEmail("user1@example.com");
        walletOne.setBalance(0.0);
        walletOne.setIsEnable(true);

        Wallet walletTwo = new Wallet();
        walletTwo.setEmail("user2@example.com");
        walletTwo.setBalance(0.0);
        walletTwo.setIsEnable(true);

        List<Wallet> wallets = Arrays.asList(walletOne, walletTwo);

        given(walletRepository.findAll()).willReturn(wallets);

        mockMvc.perform(get("/api/users/wallet/findall"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wallets", hasSize(2)))
                .andExpect(jsonPath("$.wallets[0].email", is("user1@example.com")))
                .andExpect(jsonPath("$.wallets[1].email", is("user2@example.com")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void getWalletsWhenNotAdminShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/users/wallet/findall"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBalanceWhenWalletExistsShouldUpdateBalance() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setEmail("user@example.com");
        wallet.setBalance(100.0);

        given(walletRepository.findByEmail(anyString())).willReturn(Optional.of(wallet));

        mockMvc.perform(put("/api/users/wallet/update/balance")
                        .param("email", "user@example.com")
                        .param("balance", "200.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(200.0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBalance_whenWalletDoesNotExist_shouldReturnNotFound() throws Exception {
        given(walletRepository.findByEmail(anyString())).willReturn(Optional.empty());

        mockMvc.perform(put("/api/users/wallet/update/balance")
                        .param("email", "nonexistent@example.com")
                        .param("balance", "100.0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEnable_whenWalletExists_shouldUpdateEnableStatus() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setEmail("user@example.com");
        wallet.setIsEnable(false);

        given(walletRepository.findByEmail(anyString())).willReturn(Optional.of(wallet));

        mockMvc.perform(put("/api/users/wallet/update/enable")
                        .param("email", "user@example.com")
                        .param("isEnable", "true")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isEnable", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateEnable_whenWalletDoesNotExist_shouldReturnNotFound() throws Exception {
        given(walletRepository.findByEmail(anyString())).willReturn(Optional.empty());

        mockMvc.perform(put("/api/users/wallet/update/enable")
                        .param("email", "nonexistent@example.com")
                        .param("isEnable", "true")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetWallet_whenWalletExists_shouldResetBalance() throws Exception {
        Wallet wallet = new Wallet();
        wallet.setEmail("user@example.com");
        wallet.setBalance(100.0);

        given(walletRepository.findByEmail(anyString())).willReturn(Optional.of(wallet));

        mockMvc.perform(put("/api/users/wallet/reset")
                        .param("email", "user@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance", is(0.0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void resetWallet_whenWalletDoesNotExist_shouldReturnNotFound() throws Exception {
        given(walletRepository.findByEmail(anyString())).willReturn(Optional.empty());

        mockMvc.perform(put("/api/users/wallet/reset")
                        .param("email", "nonexistent@example.com"))
                .andExpect(status().isNotFound());
    }

}

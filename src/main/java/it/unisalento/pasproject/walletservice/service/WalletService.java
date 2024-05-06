package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.WalletDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletService {

    public WalletDTO getWalletDTO(Wallet wallet) {
        WalletDTO walletDTO = new WalletDTO();
        Optional.ofNullable(wallet.getEmail()).ifPresent(walletDTO::setEmail);
        Optional.of(wallet.getBalance()).ifPresent(walletDTO::setBalance);
        Optional.ofNullable(wallet.getIsEnable()).ifPresent(walletDTO::setIsEnable);
        return walletDTO;
    }

    public Wallet getWallet(WalletDTO walletDTO) {
        Wallet wallet = new Wallet();
        Optional.ofNullable(walletDTO.getEmail()).ifPresent(wallet::setEmail);
        Optional.of(walletDTO.getBalance()).ifPresent(wallet::setBalance);
        Optional.ofNullable(walletDTO.getIsEnable()).ifPresentOrElse(wallet::setIsEnable, () -> wallet.setIsEnable(true));
        return wallet;
    }

}

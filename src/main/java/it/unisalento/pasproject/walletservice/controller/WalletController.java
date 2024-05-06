package it.unisalento.pasproject.walletservice.controller;

import it.unisalento.pasproject.walletservice.dto.WalletDTO;
import it.unisalento.pasproject.walletservice.dto.WalletListDTO;
import it.unisalento.pasproject.walletservice.exceptions.WalletNotFoundException;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/users/wallet")
public class WalletController {

    private final WalletService walletService;

    private final WalletRepository walletRepository;

    @Autowired
    public WalletController(WalletService walletService, WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
        this.walletService = walletService;
    }

    @GetMapping(value="/find")
    public WalletDTO getWallet(@RequestParam String email) throws WalletNotFoundException {
        Optional<WalletDTO> walletDTO = walletRepository.findByEmail(email).map(walletService::getWalletDTO);

        if(walletDTO.isEmpty()){
            throw new WalletNotFoundException();
        }

        return walletDTO.get();

    }

    @GetMapping(value="/findall")
    public WalletListDTO getWallets() {
        WalletListDTO walletListDTO = new WalletListDTO();
        walletListDTO.setWallets(walletRepository.findAll().stream().map(walletService::getWalletDTO).toList());
        return walletListDTO;
    }

    @PostMapping(value="/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WalletDTO addWallet(@RequestBody WalletDTO walletDTO) {
        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }

    @PostMapping(value="/add")
    public WalletDTO addWallet(@RequestParam String email, @RequestParam double balance) {
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setEmail(email);
        walletDTO.setBalance(balance);
        walletDTO.setIsEnable(true);
        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }

    @PutMapping(value="/update/balance")
    public WalletDTO updateBalance(@RequestParam String email, @RequestParam double balance) throws WalletNotFoundException {
        WalletDTO walletDTO = walletRepository.findByEmail(email).map(walletService::getWalletDTO).orElseThrow(WalletNotFoundException::new);
        walletDTO.setBalance(balance);
        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }

    @PutMapping(value="/update/enable")
    public WalletDTO updateEnable(@RequestParam String email, @RequestParam boolean isEnable) throws WalletNotFoundException {
        WalletDTO walletDTO = walletRepository.findByEmail(email).map(walletService::getWalletDTO).orElseThrow(WalletNotFoundException::new);
        walletDTO.setIsEnable(isEnable);
        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }

    @PutMapping(value="/reset")
    public WalletDTO resetWallet(@RequestParam String email) throws WalletNotFoundException {
        WalletDTO walletDTO = walletRepository.findByEmail(email).map(walletService::getWalletDTO).orElseThrow(WalletNotFoundException::new);
        walletDTO.setBalance(0.0);
        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }



}

package it.unisalento.pasproject.walletservice.controller;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.WalletDTO;
import it.unisalento.pasproject.walletservice.dto.WalletListDTO;
import it.unisalento.pasproject.walletservice.exceptions.WalletNotFoundException;
import it.unisalento.pasproject.walletservice.exceptions.WrongUserException;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import it.unisalento.pasproject.walletservice.service.UserCheckService;
import it.unisalento.pasproject.walletservice.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import static it.unisalento.pasproject.walletservice.security.SecurityConstants.*;

@RestController
@RequestMapping("/api/users/wallet")
public class WalletController {

    private final WalletService walletService;
    private final UserCheckService userCheckService;

    private final WalletRepository walletRepository;

    @Autowired
    public WalletController(WalletService walletService, WalletRepository walletRepository, UserCheckService userCheckService) {
        this.walletRepository = walletRepository;
        this.walletService = walletService;
        this.userCheckService = userCheckService;
    }

    @GetMapping(value="/find")
    public WalletDTO getWallet(@RequestParam(required = false) String email) throws WalletNotFoundException {

        //Fallback to current session user
        if (email == null){
            email = userCheckService.getCurrentUserEmail();
        }

        if (Boolean.TRUE.equals(!userCheckService.isCorrectUser(email))
                && Boolean.TRUE.equals(!userCheckService.isAdministrator())) {
            throw new WrongUserException("User not allowed to access wallet owner: " + email);
        }

        Optional<WalletDTO> walletDTO = walletRepository.findByEmail(email).map(walletService::getWalletDTO);

        if(walletDTO.isEmpty()){
            throw new WalletNotFoundException("Wallet not found for owner: " + email);
        }

        return walletDTO.get();

    }

    @GetMapping(value="/findall")
    @Secured({ROLE_ADMIN})
    public WalletListDTO getWallets() {
        WalletListDTO walletListDTO = new WalletListDTO();
        walletListDTO.setWallets(walletRepository.findAll().stream().map(walletService::getWalletDTO).toList());
        return walletListDTO;
    }

    /**
     * Add a new wallet for the user
     * @param walletDTO the wallet to add
     * @return the wallet added
     */
    @PostMapping(value="/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public WalletDTO addWallet(@RequestBody WalletDTO walletDTO) {

        if (Boolean.TRUE.equals(!userCheckService.isCorrectUser(walletDTO.getEmail()))
                && Boolean.TRUE.equals(!userCheckService.isAdministrator())) {
            throw new WrongUserException("User not allowed to create wallet for a different owner: " + walletDTO.getEmail());
        } // Se è un admin può creare wallet per altri users

        Optional<Wallet> wallet = walletRepository.findByEmail(walletDTO.getEmail());

        // If the wallet already exists, return it
        if ( wallet.isPresent() ) {
            return walletService.getWalletDTO(wallet.get());
        }

        //Prevent users from setting their own balance
        if(Boolean.FALSE.equals(userCheckService.isAdministrator())){
            walletDTO.setBalance(0.0);
        }

        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }

    @PostMapping(value="/add")
    public WalletDTO addWallet(@RequestParam String email, @RequestParam double balance) {

        if (Boolean.TRUE.equals(!userCheckService.isCorrectUser(email))
                && Boolean.TRUE.equals(!userCheckService.isAdministrator())) {
            throw new WrongUserException("User not allowed to create wallet for a different owner: " + email);
        }

        Optional<Wallet> wallet = walletRepository.findByEmail(email);

        if ( wallet.isPresent() ) {
            return walletService.getWalletDTO(wallet.get());
        }

        WalletDTO walletDTO = new WalletDTO();
        //Prevent users from setting their own balance
        if(Boolean.FALSE.equals(userCheckService.isAdministrator())){
            walletDTO.setBalance(0.0);
        } else {
            walletDTO.setBalance(balance);
        }
        walletDTO.setEmail(email);
        walletDTO.setIsEnable(true);

        return walletService.getWalletDTO(walletRepository.save(walletService.getWallet(walletDTO)));
    }

    @PutMapping(value="/update/balance")
    @Secured({ROLE_ADMIN})
    public WalletDTO updateBalance(@RequestParam String email, @RequestParam double balance) throws WalletNotFoundException {

        Optional<Wallet> wallet = walletRepository.findByEmail(email);

        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found for owner: " + email);
        }

        Wallet ret = wallet.get();
        ret.setBalance(balance);

        return walletService.getWalletDTO(walletRepository.save(ret));
    }

    @PutMapping(value="/update/enable")
    @Secured({ROLE_ADMIN})
    public WalletDTO updateEnable(@RequestParam String email, @RequestParam boolean isEnable) throws WalletNotFoundException {

        Optional<Wallet> wallet = walletRepository.findByEmail(email);

        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found for owner: " + email);
        }

        Wallet ret = wallet.get();
        ret.setIsEnable(isEnable);

        return walletService.getWalletDTO(walletRepository.save(ret));
    }

    @PutMapping(value="/reset")
    @Secured({ROLE_ADMIN})
    public WalletDTO resetWallet(@RequestParam String email) throws WalletNotFoundException {

        Optional<Wallet> wallet = walletRepository.findByEmail(email);

        if (wallet.isEmpty()) {
            throw new WalletNotFoundException("Wallet not found for owner: " + email);
        }

        Wallet ret = wallet.get();
        ret.setBalance(0.0);

        return walletService.getWalletDTO(walletRepository.save(ret));
    }


}

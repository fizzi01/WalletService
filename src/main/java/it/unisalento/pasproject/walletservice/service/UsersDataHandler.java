package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import it.unisalento.pasproject.walletservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

import static it.unisalento.pasproject.walletservice.security.SecurityConstants.ROLE_ADMIN;
import static it.unisalento.pasproject.walletservice.security.SecurityConstants.ROLE_UTENTE;
import static it.unisalento.pasproject.walletservice.security.WalletConstants.UTENTE_INITIAL_BALANCE;

@Service
public class UsersDataHandler {
    // This class is used to handle the data of the users.
    // It is used to retrieve the data of the users when they are created.
    private final WalletRepository walletRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersDataHandler.class);

    @Autowired
    public UsersDataHandler(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.userData.name}")
    public void receiveMessage(UserDTO userDTO) {
        try {
            LOGGER.info("Received message: {}", userDTO);

            if (ROLE_ADMIN.equalsIgnoreCase(userDTO.getRole())) {
                LOGGER.info("Admin user detected, skipping creation");
                return;
            }

            //Check if the user exists
            Optional<Wallet> walletOptional = walletRepository.findByEmail(userDTO.getEmail());

            if (walletOptional.isPresent()) {
                LOGGER.info("User already exists");
                return;
            }

            if (userDTO.getRole().equalsIgnoreCase("admin")) {
                LOGGER.info("Admin user detected, skipping creation");
                return;
            }

            double balance = 0.0;

            if (ROLE_UTENTE.equalsIgnoreCase(userDTO.getRole())) {
                balance = UTENTE_INITIAL_BALANCE;
            }

            Wallet wallet = new Wallet();
            wallet.setEmail(userDTO.getEmail());
            wallet.setBalance(balance);
            wallet.setIsEnable(true);

            walletRepository.save(wallet);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}

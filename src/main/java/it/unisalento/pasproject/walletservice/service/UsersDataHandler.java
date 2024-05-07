package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import it.unisalento.pasproject.walletservice.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        LOGGER.info("Received message: {}", userDTO);

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

        Wallet wallet = new Wallet();
        wallet.setEmail(userDTO.getEmail());
        wallet.setBalance(0.0);
        wallet.setIsEnable(true);

        walletRepository.save(wallet);
    }
}

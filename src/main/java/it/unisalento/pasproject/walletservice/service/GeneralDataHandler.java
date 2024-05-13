package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.GeneralDataDTO;
import it.unisalento.pasproject.walletservice.dto.UserDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This class is used to handle the general data.
 * It is used to retrieve all the data that should have a wallet. (Rewards, ...)
 */
@Service
public class GeneralDataHandler {
    private final WalletRepository walletRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(GeneralDataHandler.class);

    @Autowired
    public GeneralDataHandler(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @RabbitListener(queues = "${rabbitmq.queue.receiveData.name}")
    public void receiveMessage(GeneralDataDTO dataDTO) {

        LOGGER.info("Received message: {}", dataDTO);

        //Check if the user exists
        Optional<Wallet> walletOptional = walletRepository.findByEmail(dataDTO.getId());

        if (walletOptional.isPresent()) {
            LOGGER.info("User already exists");
            return;
        }

        Wallet wallet = new Wallet();
        wallet.setEmail(dataDTO.getId());
        wallet.setBalance(0.0);
        wallet.setIsEnable(true);

        walletRepository.save(wallet);
    }
}

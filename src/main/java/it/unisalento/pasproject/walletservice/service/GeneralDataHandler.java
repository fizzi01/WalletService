package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.GeneralDataDTO;
import it.unisalento.pasproject.walletservice.dto.GeneralRequestDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static it.unisalento.pasproject.walletservice.security.WalletConstants.UTENTE_INITIAL_BALANCE;

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
        try {
            LOGGER.debug("Received message: {}", dataDTO);

            //Check if the user exists
            Optional<Wallet> walletOptional = walletRepository.findByEmail(dataDTO.getId());

            if (walletOptional.isPresent()) {
                LOGGER.error("User already exists");
                return;
            }

            Wallet wallet = new Wallet();
            wallet.setEmail(dataDTO.getId());
            wallet.setBalance(0.0);
            wallet.setIsEnable(true);

            walletRepository.save(wallet);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queue.generalRequest.name}")
    public void receiveRequest(GeneralRequestDTO requestDTO) {
        try{
            LOGGER.debug("Received request: {}", requestDTO);

            //Check if the user exists
            Optional<Wallet> walletOptional = walletRepository.findByEmail(requestDTO.getEmail());

            if (walletOptional.isEmpty()) {
                return;
            }

            Wallet wallet = walletOptional.get();

            switch (requestDTO.getRequestType()) {
                case GeneralRequestDTO.RequestType.DISABLE:
                    wallet.setIsEnable(false);
                    break;
                case GeneralRequestDTO.RequestType.ENABLE:
                    wallet.setIsEnable(true);
                    break;
                case GeneralRequestDTO.RequestType.DEPOSIT:
                    wallet.setBalance(wallet.getBalance() + requestDTO.getAmount());
                    break;
                case GeneralRequestDTO.RequestType.REFILL:
                    wallet.setBalance(UTENTE_INITIAL_BALANCE);
                    break;
                case GeneralRequestDTO.RequestType.SUBTRACT:
                    if((wallet.getBalance() - requestDTO.getAmount()) <= 0){
                        wallet.setBalance(0.0);
                    }else{
                        wallet.setBalance(wallet.getBalance() - requestDTO.getAmount());
                    }
                    break;
                default:
                    LOGGER.error("Request type not found");
                    return;
            }

            walletRepository.save(wallet);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }
}

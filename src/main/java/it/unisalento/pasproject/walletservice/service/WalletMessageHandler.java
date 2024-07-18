package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.MessageDTO;
import it.unisalento.pasproject.walletservice.dto.RequestTransactionDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletMessageHandler {


    private final WalletRepository walletRepository;

    @Autowired
    public WalletMessageHandler(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WalletMessageHandler.class);


    @RabbitListener(queues = "${rabbitmq.queue.receiveTransaction.name}")
    public MessageDTO receiveTransaction(RequestTransactionDTO message) {

        String mittente = message.getSenderEmail();
        String destinatario = message.getReceiverEmail();
        double importo = message.getAmount();
        String transactionId = message.getId();

        //Controlla se il mittente ha abbastanza soldi
        Optional<Wallet> sender = walletRepository.findByEmail(mittente);

        if (sender.isEmpty()) {
            LOGGER.info("Transaction failed: sender not found");
            return new MessageDTO(transactionId, 400);
        }

        Wallet senderWallet = sender.get();

        if(senderWallet.getBalance() < importo || !senderWallet.getIsEnable()) {
            LOGGER.info("Transaction failed: Sender not enough money");
            LOGGER.info("Sender balance: {}", senderWallet.getBalance());
            return new MessageDTO(transactionId, 400);
        }

        //Controlla se il destinatario è abilitato
        Optional<Wallet> receiver = walletRepository.findByEmail(destinatario);

        if (receiver.isEmpty()){
            LOGGER.info("Transaction failed: receiver not found");
            return new MessageDTO(transactionId, 400);
        }

        Wallet receiverWallet = receiver.get();

        if(!receiver.get().getIsEnable()) {
            LOGGER.info("Transaction failed: receiver not enabled");
            return new MessageDTO(transactionId, 400);
        }

        //Se il mittente ha abbastanza soldi e il destinatario è abilitato, esegui la transazione
        senderWallet.setBalance(senderWallet.getBalance() - importo);
        receiverWallet.setBalance(receiverWallet.getBalance() + importo);

        try{
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
        } catch (Exception e) {
            return new MessageDTO(transactionId, 400);
        }

        LOGGER.info("Transaction {} completed", transactionId);
        return new MessageDTO(transactionId, 200);
    }

}

package it.unisalento.pasproject.walletservice.service;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import it.unisalento.pasproject.walletservice.dto.MessageDTO;
import it.unisalento.pasproject.walletservice.dto.RequestTransactionDTO;
import it.unisalento.pasproject.walletservice.repositories.WalletRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WalletMessageHandler {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletService walletService;

    @RabbitListener(queues = "${rabbitmq.queue.receiveTransaction.name}")
    public MessageDTO receiveTransaction(RequestTransactionDTO message) {

        String mittente = message.getSenderEmail();
        String destinatario = message.getReceiverEmail();
        double importo = message.getAmount();
        String transactionId = message.getId();

        //Controlla se il mittente ha abbastanza soldi
        Optional<Wallet> sender = walletRepository.findById(mittente);

        if(sender.isEmpty() || sender.get().getBalance() < importo || !sender.get().getIsEnable()) {
            return new MessageDTO(transactionId, 400);
        }

        //Controlla se il destinatario è abilitato
        Optional<Wallet> receiver = walletRepository.findById(destinatario);

        if(receiver.isEmpty() || !receiver.get().getIsEnable()) {
            return new MessageDTO(transactionId, 400);
        }

        //Se il mittente ha abbastanza soldi e il destinatario è abilitato, esegui la transazione
        Wallet senderWallet = sender.get();
        Wallet receiverWallet = receiver.get();

        senderWallet.setBalance(senderWallet.getBalance() - importo);
        receiverWallet.setBalance(receiverWallet.getBalance() + importo);

        try{
            walletRepository.save(senderWallet);
            walletRepository.save(receiverWallet);
        } catch (Exception e) {
            return new MessageDTO(transactionId, 400);
        }

        return new MessageDTO(transactionId, 200);
    }

}

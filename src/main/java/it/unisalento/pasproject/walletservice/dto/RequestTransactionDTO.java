package it.unisalento.pasproject.walletservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestTransactionDTO {
    private String id;
    private String senderEmail;
    private String receiverEmail;

    private double amount;
}

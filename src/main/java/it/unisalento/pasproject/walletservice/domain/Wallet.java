package it.unisalento.pasproject.walletservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "wallet")
public class Wallet {
    private String id;
    private String email;
    private double balance;
    private Boolean isEnable;
}

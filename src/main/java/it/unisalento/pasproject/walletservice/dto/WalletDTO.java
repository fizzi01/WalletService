package it.unisalento.pasproject.walletservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalletDTO {

    private String email;
    private double balance;
    private Boolean isEnable;

    public WalletDTO() {
    }

    public WalletDTO(String email, double balance, Boolean isEnable) {
        this.email = email;
        this.balance = balance;
        this.isEnable = isEnable;
    }
}

package it.unisalento.pasproject.walletservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GeneralRequestDTO {

    public enum RequestType {
        DEPOSIT,
        REFILL,
        SUBTRACT,
        DISABLE,
        ENABLE
    }

    private String email;
    private RequestType requestType;
    private double amount;
}

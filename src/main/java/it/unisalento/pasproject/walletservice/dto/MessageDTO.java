package it.unisalento.pasproject.walletservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageDTO {

    //Messaggio di risposta
    private String response;

    //Codice di risposta stile http (200, 404, 500, ...)
    private int code;

    public MessageDTO(String response, int code) {
        this.response = response;
        this.code = code;
    }
}

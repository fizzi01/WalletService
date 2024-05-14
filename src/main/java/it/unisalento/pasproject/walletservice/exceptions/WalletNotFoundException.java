package it.unisalento.pasproject.walletservice.exceptions;

import org.springframework.http.HttpStatus;


public class WalletNotFoundException extends CustomErrorException {

    public WalletNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}

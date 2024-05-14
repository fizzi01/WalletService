package it.unisalento.pasproject.walletservice.exceptions;

import org.springframework.http.HttpStatus;

public class WrongUserException extends CustomErrorException{

    public WrongUserException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

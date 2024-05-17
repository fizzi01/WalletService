package it.unisalento.pasproject.walletservice.exceptions;

import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomErrorException{
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}

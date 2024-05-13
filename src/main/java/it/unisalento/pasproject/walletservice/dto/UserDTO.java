package it.unisalento.pasproject.walletservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
public class UserDTO {
    private String email;
    private String name;
    private String surname;
    private LocalDateTime registrationDate;
    private String role;
}

package it.unisalento.pasproject.walletservice.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class WalletListDTO {
    private List<WalletDTO> wallets;

}

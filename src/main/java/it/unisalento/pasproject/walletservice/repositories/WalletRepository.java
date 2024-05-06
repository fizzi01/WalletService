package it.unisalento.pasproject.walletservice.repositories;

import it.unisalento.pasproject.walletservice.domain.Wallet;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface WalletRepository extends MongoRepository<Wallet, String> {
    Optional<Wallet> findByEmail(String email);
}

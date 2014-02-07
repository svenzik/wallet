package com.playtech.wallet.repository;

import com.playtech.wallet.domain.transactions.PlayerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PlayerTransactionJpaRepository extends JpaRepository<PlayerTransaction, Long>{

    /**
     * Search the transaction by identifier
     * @param transaction UUID of transaction
     * @return PlayerTransaction
     */
    public PlayerTransaction findByTransactionId(UUID transaction);

}

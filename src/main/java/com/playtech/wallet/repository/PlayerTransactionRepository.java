package com.playtech.wallet.repository;

import com.playtech.wallet.domain.transactions.PlayerTransaction;

import java.util.UUID;

/**
 * Persisting Player domain object
 */
public interface PlayerTransactionRepository {

    /**
     * Search the transaction
     * @param transaction UUID of transaction
     * @return PlayerTransaction
     */
    PlayerTransaction findByTransactionId(UUID transaction);

    /**
     * Persist data to database
     * @param transaction PlayerTransaction instance to persist
     * @return persisted PlayerTransaction
     */
    PlayerTransaction save(PlayerTransaction transaction);

}

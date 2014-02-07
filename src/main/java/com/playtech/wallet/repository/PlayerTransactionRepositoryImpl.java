package com.playtech.wallet.repository;

import com.playtech.wallet.domain.transactions.PlayerTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class PlayerTransactionRepositoryImpl implements PlayerTransactionRepository {

    @Autowired
    public PlayerTransactionRepositoryImpl(PlayerTransactionJpaRepository playerTransactionJpaRepository) {
        this.crud = playerTransactionJpaRepository;
    }

    private PlayerTransactionJpaRepository crud;

    @Override
    public PlayerTransaction findByTransactionId(UUID transaction) {
        return crud.findByTransactionId(transaction);
    }

    @Override
    public PlayerTransaction save(PlayerTransaction transaction) {
        return crud.save(transaction);
    }

}

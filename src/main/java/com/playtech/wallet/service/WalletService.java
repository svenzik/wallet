package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.spring.controller.WalletChangeMessage;
import com.playtech.wallet.spring.controller.WalletChangeResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class WalletService {

    @Autowired
    public WalletService(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    private PlayerRepository playerRepository;

    //for syncronizing impl
    private static final Object lockForSyncronizing = new Object();


    public WalletChangeResult changeBalance(final WalletChangeMessage walletChangeMessage) {

        Player player;

        player = mergeToPlayerAndPersistSyncronized(walletChangeMessage);

        WalletChangeResult result = new WalletChangeResult();
        result.setTransactionId(walletChangeMessage.getTransactionId());
        result.setBalanceVersion(player.getVersion());
        result.setBalanceChange(walletChangeMessage.getBalanceChange());
        result.setTotalBalance(player.getBalance());
        result.setErrorCode(null);

        return result;
    }


    /**
     * saves message to db using syncronised construct
     * @param walletChangeMessage message
     * @return Last state of user
     */
    private Player mergeToPlayerAndPersistSyncronized(WalletChangeMessage walletChangeMessage) {

        Player player;
        //who writes this stuff
        synchronized (lockForSyncronizing) {
            player = mergeToPlayerAndPersist(walletChangeMessage);
        }
        return player;
    }

    /**
     * Persist ChangeMessage to db. THis is not threadsafw
     * @param walletChangeMessage Message to be handeled
     * @return Updated Player
     */
    private Player mergeToPlayerAndPersist(WalletChangeMessage walletChangeMessage) {

        Player player = playerRepository.findByUsername(walletChangeMessage.getUsername());

        if (player == null) {
            throw new IllegalArgumentException("Username does not exist in database");
        }

        //transaction
        BigDecimal balanceChange = walletChangeMessage.getBalanceChange();
        player.changeBalance(balanceChange);
        player = playerRepository.save(player);
        //transaction end

        return player;
    }

}

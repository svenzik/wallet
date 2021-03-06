package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.exceptions.BalanceLessThenZeroException;
import com.playtech.wallet.domain.messages.WalletChangeResultStatus;
import com.playtech.wallet.domain.transactions.PlayerTransaction;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.domain.messages.WalletChangeResult;
import com.playtech.wallet.repository.PlayerTransactionRepository;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * For persisting Player messages
 */
@Service
public class WalletService {

    //AOP
    public WalletService() {
    }

    @Autowired
    public WalletService(PlayerRepository playerRepository, PlayerTransactionRepository playerTransactionRepository) {
        this.playerRepository = playerRepository;
        this.playerTransactionRepository = playerTransactionRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(WalletService.class);

    private PlayerRepository playerRepository;
    private PlayerTransactionRepository playerTransactionRepository;

    public WalletChangeResult changeBalance(final WalletChangeMessage walletChangeMessage) {

        WalletChangeResult result = new WalletChangeResult();
        result.setTransactionId(walletChangeMessage.getTransactionId());
        result.setBalanceChange(walletChangeMessage.getBalanceChange());

        //try to do the transaction
        try {
            Player player = playerRepository.findByUsername(walletChangeMessage.getUsername());
            //set current values
            result.setBalanceVersion(player.getVersion());
            result.setTotalBalance(player.getBalance());

            try {
                player = mergeToPlayerAndPersist(player, walletChangeMessage);
                //set updated values
                result.setBalanceVersion(player.getVersion());
                result.setTotalBalance(player.getBalance());

            } catch(BalanceLessThenZeroException balanceException) {

                result.setErrorCode(WalletChangeResultStatus.PLAYER_BALANCE_LESS_THAN_ZERO);
                logger.warn(balanceException.getMessage());

            } catch(RepeatingTransaction repeatingTransactionException) {

                result.setErrorCode(WalletChangeResultStatus.REPEATING_TRANSACTION);

            } catch(OptimisticLockingFailureException lockingException) {

                result.setErrorCode(WalletChangeResultStatus.OPTIMISTIC_LOCKING_EXCEPTION);
                logger.info("Concurrent modification of player on walletChangeMessage {}: {}"
                        , walletChangeMessage, lockingException.toString());
            }



        } catch(PlayerNotFoundException e) {

            result.setErrorCode(WalletChangeResultStatus.NO_SUCH_PLAYER);
            logger.error(e.getMessage());

        }

        return result;
    }

    /**
     * Persist WalletChangeMessage and transaction to db.
     * @param walletChangeMessage WalletChangeMessage to be handeled
     * @return Updated Player or null, if user does not exist
     */
    private Player mergeToPlayerAndPersist(Player player, WalletChangeMessage walletChangeMessage)
            throws PlayerNotFoundException {

        if (playerTransactionRepository.findByTransactionId(walletChangeMessage.getTransactionId()) != null){
            throw new RepeatingTransaction();
        }

        //transaction
        BigDecimal balanceChange = walletChangeMessage.getBalanceChange();
        player.changeBalance(balanceChange);
        player = playerRepository.save(player);

        playerTransactionRepository.save(new PlayerTransaction(walletChangeMessage.getTransactionId()));
        //transaction end

        return player;
    }

    /**
     * If transaction has allready taken place, then this is thrown
     */
    private class RepeatingTransaction extends RuntimeException { }

}

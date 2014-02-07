package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.PlayerFactory;
import com.playtech.wallet.domain.messages.WalletChangeResultStatus;
import com.playtech.wallet.domain.transactions.PlayerTransaction;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.domain.messages.WalletChangeResult;
import com.playtech.wallet.repository.PlayerTransactionRepository;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalletServiceTest {

    private PlayerRepository playerRepository = new PlayerRepository() {

        private Player player = PlayerFactory.createNewPlayer("username");
        private final String lockUsername = WalletChangeResultStatus.OPTIMISTIC_LOCKING_EXCEPTION.toString();

        @Override
        public Player findByUsername(String username) throws PlayerNotFoundException{
            if (player.getUsername().equals(username)) {
                return player;
            }
            if (username.equals(lockUsername)) {
                return PlayerFactory.createNewPlayer(lockUsername);
            }
            throw new PlayerNotFoundException(username);
        }

        @Override
        public Player save(Player entity) {
            if (lockUsername.equals(entity.getUsername())) {
                throw new OptimisticLockingFailureException("TEST EXCEPTION");
            }
            Player tmpPlayer = PlayerFactory.createNewPlayer(entity.getUsername());
            tmpPlayer.changeBalance(entity.getBalance());
            player = tmpPlayer;
            return player;
        }

        @Override
        public List<Player> findAll() {
            List<Player> l = new ArrayList<Player>(1);
            l.add(player);
            return l;
        }


    };

    private PlayerTransactionRepository transactionRepository = new PlayerTransactionRepository() {

        private List<UUID> transactionTable = new ArrayList<UUID>();

        @Override
        public PlayerTransaction findByTransactionId(UUID transaction) {
            for (UUID uuid : transactionTable) {
                if (uuid.equals(transaction)) {
                    return new PlayerTransaction(uuid);
                }
            }
            return null;
        }

        @Override
        public PlayerTransaction save(PlayerTransaction transaction) {
            this.transactionTable.add(transaction.getTransactionId());
            return transaction;
        }

        @Override
        public List<PlayerTransaction> findAll() {
            return null;
        }
    };

    private WalletService walletService = new WalletService(playerRepository, transactionRepository);

    private WalletChangeMessage incomingMessage = new WalletChangeMessage();

    private Player getUser() throws PlayerNotFoundException {
        return playerRepository.findByUsername("username");
    }

    @Before
    public void setUp() throws Exception {

        BigDecimal balanceChange = BigDecimal.valueOf(11.34);
        UUID transactionId = UUID.randomUUID();

        incomingMessage.setUsername("username");
        incomingMessage.setTransactionId(transactionId);
        incomingMessage.setBalanceChange(balanceChange);

    }

    @Test
    public void testChangeBalanceOk() throws Exception {

        BigDecimal startBalance = getUser().getBalance();
        BigDecimal balanceChange = incomingMessage.getBalanceChange();

        WalletChangeResult result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(WalletChangeResultStatus.OK, result.getErrorCode());

        //calculate balance change
        BigDecimal expectedBalance = startBalance.add(balanceChange);
        Assert.assertEquals(expectedBalance, result.getTotalBalance());

        incomingMessage.setTransactionId(UUID.randomUUID());
        result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(WalletChangeResultStatus.OK, result.getErrorCode());

        //calculate 2xbalance change
        expectedBalance = expectedBalance.add(balanceChange);
        Assert.assertEquals(expectedBalance, result.getTotalBalance());

    }

    @Test
    public void testChangeBalanceRepeatingTransaction() throws Exception {

        BigDecimal startBalance = getUser().getBalance();
        BigDecimal balanceChange = incomingMessage.getBalanceChange();

        WalletChangeResult result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(result.getErrorCode(), WalletChangeResultStatus.OK);

        BigDecimal expectedBalance = startBalance.add(balanceChange);
        Assert.assertEquals(expectedBalance, result.getTotalBalance());

        result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(WalletChangeResultStatus.REPEATING_TRANSACTION, result.getErrorCode());

        //total balance must not change
        Assert.assertEquals(expectedBalance, result.getTotalBalance());

    }

    @Test
    public void testChangeBalanceOptimisticLockingException() throws Exception {

        BigDecimal startBalance = getUser().getBalance();

        incomingMessage.setUsername(WalletChangeResultStatus.OPTIMISTIC_LOCKING_EXCEPTION.toString());

        WalletChangeResult result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(WalletChangeResultStatus.OPTIMISTIC_LOCKING_EXCEPTION, result.getErrorCode());

        //total balance must not change
        Assert.assertEquals(startBalance, result.getTotalBalance());

    }
    @Test
    public void testChangeBalanceNoSuchPlayer() throws Exception {

        incomingMessage.setUsername("NO_SUCH_USERERNAME");

        WalletChangeResult result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(WalletChangeResultStatus.NO_SUCH_PLAYER, result.getErrorCode());

        //non existin users balance is null
        Assert.assertNull(result.getTotalBalance());

    }


    @Test
    public void testChangeBalanceMoreThenAvailable() throws Exception {

        BigDecimal startBalance = getUser().getBalance();

        incomingMessage.setBalanceChange(startBalance.subtract(BigDecimal.ONE));

        WalletChangeResult result = walletService.changeBalance(incomingMessage);

        assertAllwaysTrueWalletService(incomingMessage, result);
        Assert.assertEquals(WalletChangeResultStatus.PLAYER_BALANCE_LESS_THAN_ZERO, result.getErrorCode());

        Assert.assertEquals(startBalance, result.getTotalBalance());

    }

    private void assertAllwaysTrueWalletService(WalletChangeMessage incomingMessage, WalletChangeResult result) {
        Assert.assertEquals(incomingMessage.getTransactionId(), result.getTransactionId());
        Assert.assertEquals(incomingMessage.getBalanceChange(), result.getBalanceChange());
    }
}

package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.PlayerFactory;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.domain.messages.WalletChangeResult;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WalletServiceTest extends TestCase {


    private PlayerRepository repository = new PlayerRepository() {

        private Player player = PlayerFactory.createNewPlayer("username");

        @Override
        public Player findByUsername(String username) {
            return player;
        }

        @Override
        public Player save(Player entity) {
            return player;
        }

        @Override
        public List<Player> findAll() {
            List<Player> l = new ArrayList<Player>(1);
            l.add(player);
            return l;
        }


    };

    private WalletService walletService = new WalletService(repository);

    private Player getUser() {
        return repository.findByUsername("username");
    }

    @Test
    public void testChangeBalance() throws Exception {

        BigDecimal startBalance = getUser().getBalance();

        UUID transactionId = UUID.randomUUID();
        BigDecimal balanceChange = new BigDecimal(11.34);

        WalletChangeMessage message = new WalletChangeMessage();
        //change values
        message.setUsername("username");
        message.setTransactionId(transactionId);
        message.setBalanceChange(balanceChange);

        WalletChangeResult result = walletService.changeBalance(message);

        Assert.assertEquals(transactionId, result.getTransactionId());
        Assert.assertNull(result.getErrorCode());
        Assert.assertEquals(balanceChange, result.getBalanceChange());

        BigDecimal expectedBalance = startBalance.add(balanceChange);
        Assert.assertEquals(expectedBalance, result.getTotalBalance());
    }



}

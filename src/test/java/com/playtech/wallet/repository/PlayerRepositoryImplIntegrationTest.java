package com.playtech.wallet.repository;

import com.playtech.wallet.AbstractBaseIntegrationTest;
import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.PlayerFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicLong;


public class PlayerRepositoryImplIntegrationTest extends AbstractBaseIntegrationTest {

    private final String PLAYER_NAME_PREFIX = "player-";
    private final AtomicLong usernameCounter = new AtomicLong();

    @Autowired
    private PlayerRepository playerRepository;

//    @Before
//    public void setup() {
//
//    }

    @Test
    public void testFindByUsername() throws Exception {

        Player player = PlayerFactory.createNewPlayer("testFindByUsername");
        playerRepository.save(player);

        player = playerRepository.findByUsername("testFindByUsername");
        Assert.assertNotNull(player);

    }

    @Test
    public void testSave() throws Exception {
        Player newPlayer = PlayerFactory.createNewPlayer("testSave");
        newPlayer.changeBalance(BigDecimal.ONE);

        Player savedPlayer = playerRepository.save(newPlayer);
        Assert.assertEquals(BigDecimal.ONE, savedPlayer.getBalance());
        Assert.assertEquals(new Long(0L), savedPlayer.getVersion());

        savedPlayer.changeBalance(BigDecimal.ONE);

        savedPlayer = playerRepository.save(savedPlayer);
        Assert.assertEquals(new Long(1L), savedPlayer.getVersion());
    }

    @Test
    public void testFindAll() throws Exception {

        final int PLAYERS_COUNT = 5;
        for (int i = 0; i < PLAYERS_COUNT; i++) {
            Player newPlayer = PlayerFactory.createNewPlayer("player-"+i);
            playerRepository.save(newPlayer);
        }

        Assert.assertTrue(PLAYERS_COUNT <= playerRepository.findAll().size());
    }

    @Test(expected=OptimisticLockingFailureException.class)
    public void testOptimisticLock() throws Exception {
        Player newPlayer = PlayerFactory.createNewPlayer("testOptimisticLock");
        newPlayer = playerRepository.save(newPlayer);

        Assert.assertEquals(new Long(0L), newPlayer.getVersion());
        newPlayer.changeBalance(BigDecimal.ONE);

        Player savedPlayer = playerRepository.save(newPlayer);
        Assert.assertEquals(new Long(1L), savedPlayer.getVersion());

        //SHOULD THROW OptimisticLockException
        playerRepository.save(newPlayer);
    }


}

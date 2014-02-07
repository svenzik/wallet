package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.PlayerFactory;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class WalletServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PlayerRepository playerRepository;

    private final String PLAYER_USERNAME = "WalletServiceIntegrationTest";

    private final static List<Exception> exceptionList = new ArrayList<Exception>();

    private final AtomicLong sum = new AtomicLong();
    private final AtomicLong count = new AtomicLong();


    private Player getAndResetPlayerFromRepository() {

        Player player;
        try {
            player = playerRepository.findByUsername(PLAYER_USERNAME);
        } catch (PlayerNotFoundException e) {
            player = PlayerFactory.createNewPlayer(PLAYER_USERNAME);
            playerRepository.save(player);
        }

        //make the balance 0.53
        player.changeBalance(BigDecimal.ZERO.subtract(player.getBalance()).add(BigDecimal.valueOf(53,2)));
        return playerRepository.save(player);
    }

    /**
     * Modifies the player
     * @throws Exception if test fails
     */
    @Test
    public void testChangeBalance() throws Exception {

        int threads = 100;
        Player playerState = getAndResetPlayerFromRepository();

        ExecutorService taskExecutor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            taskExecutor.execute(new BalanceUpdater());
        }

        taskExecutor.shutdown();

        try {
            taskExecutor.awaitTermination(120L, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new Exception("Test failed to end in time", e);
        }


        Player player = playerRepository.findByUsername(PLAYER_USERNAME);
        String format = "sum(%s)-count(%s)";

        String sumAndCountRepository = String.format(format, player.getBalance(), player.getVersion()-playerState.getVersion());
        String sumAndCountExpected = String.format(format,
                                                   playerState.getBalance().add(BigDecimal.valueOf(sum.get())),
                                                   count.get());

        assertEquals(sumAndCountExpected, sumAndCountRepository);

        assertEquals(0, exceptionList.size());
        assertThat(exceptionList, is((List<Exception>)new ArrayList<Exception>()));

    }

    private class BalanceUpdater implements Runnable {

        @Override
        public void run() {

            for (int i = 10; i>=-10; i--) {

                BigDecimal balanceChange = BigDecimal.valueOf(i);

                if (i == 0) {
                    //with 0 there is no transaction, so lets change that
                    balanceChange = BigDecimal.valueOf(13);
                }

                sum.addAndGet(balanceChange.longValue());
                count.getAndIncrement();

                UUID transactionId = UUID.randomUUID();

                WalletChangeMessage message = new WalletChangeMessage();
                message.setUsername(PLAYER_USERNAME);
                message.setTransactionId(transactionId);
                message.setBalanceChange(balanceChange);

                try {

                  getMockMvc().perform(
                          post("/wallet")
                          .contentType(MediaType.APPLICATION_JSON)
                          .content(convertObjectToJsonBytes(message)))
//                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                            .andExpect(jsonPath("$.errorCode").value("OK"))
                            .andExpect(jsonPath("$.balanceChange").value(balanceChange.intValue())) //.intValue() otherwise fails
                            .andReturn();


                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionList.add(e);
                }
            }
        }

    }

}

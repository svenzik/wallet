package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.PlayerFactory;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.domain.messages.WalletChangeMessage;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.util.MatcherAssertionErrors.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class WalletServiceIntegrationTest extends ServiceIntegrationTest {

    @Autowired
    private PlayerRepository playerRepository;

    private final String PLAYER_USERNAME = "username";

    private List<Exception> exceptionList = new ArrayList<Exception>();

    private Player getAndResetPlayerFromRepository() {
        Player player = playerRepository.findByUsername(PLAYER_USERNAME);
        if (player == null) {
            player = PlayerFactory.createNewPlayer(PLAYER_USERNAME);
            playerRepository.save(player);
        }
        //make the balance 0.53
        player.changeBalance(BigDecimal.ZERO.subtract(player.getBalance()).add(BigDecimal.valueOf(53,2)));
        return playerRepository.save(player);
    }

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

        //every thread adds 11 to balance
        assertEquals(playerState.getBalance().add(BigDecimal.valueOf(threads * 11)),
                    playerRepository.findByUsername(PLAYER_USERNAME).getBalance());

        assertThat(exceptionList, is((List<Exception>)new ArrayList<Exception>()));

    }



    private class BalanceUpdater implements Runnable {


        @Override
        public void run() {
//            BigDecimal sum = getAndResetPlayerFromRepository().getBalance();
            Long lastVersion = getAndResetPlayerFromRepository().getVersion();

            for (int i = 11; i>=-10; i--) {

                UUID transactionId = UUID.randomUUID();
                Long probableNewVersion = lastVersion + 1L;

                WalletChangeMessage message = new WalletChangeMessage();
                message.setUsername(PLAYER_USERNAME);
                message.setTransactionId(transactionId);
                message.setBalanceChange(BigDecimal.valueOf(i));


                try {

                  getMockMvc().perform(post("/wallet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(convertObjectToJsonBytes(message)))
//                            .andDo(print())
                            .andExpect(status().isOk())
                            .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                                    .andExpect(jsonPath("$.errorCode").value(nullValue()))
//                            .andExpect(content().string("errorCode"))
//                            .andExpect(jsonPath("$.balanceVersion", is(probableNewVersion)))
                            .andExpect(jsonPath("$.balanceChange").value(BigDecimal.valueOf(i).intValue())) //.intValue() otherwise fails
                            .andReturn();


                } catch (Exception e) {
                    e.printStackTrace();
                    exceptionList.add(e);
//                    throw new RuntimeException(e);
                }




            }
        }

    }

}

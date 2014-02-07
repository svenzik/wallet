package com.playtech.wallet.service;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.domain.PlayerFactory;
import com.playtech.wallet.repository.PlayerRepository;
import com.playtech.wallet.domain.messages.WalletChangeMessage;
import com.playtech.wallet.repository.PlayerTransactionRepository;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class WalletServiceIntegrationTest extends AbstractServiceIntegrationTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private PlayerTransactionRepository playerTransactionRepository;

    private final String PLAYER_USERNAME = "WalletServiceIntegrationTest";

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

        Player playerState = getAndResetPlayerFromRepository();
        int transactionCountState = playerTransactionRepository.findAll().size();

        for (int i = 10; i>=-10; i--) {

            BigDecimal balanceChange = BigDecimal.valueOf(Math.round(Math.random() * 100));
            UUID transactionId = UUID.randomUUID();

            WalletChangeMessage message = new WalletChangeMessage();
            message.setUsername(PLAYER_USERNAME);
            message.setTransactionId(transactionId);
            message.setBalanceChange(balanceChange);

            getMockMvc().perform(
                    post("/wallet")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(convertObjectToJsonBytes(message)))
                    //                            .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.transactionId").value(transactionId.toString()))
                    .andExpect(jsonPath("$.errorCode").value("OK"))
                    .andExpect(jsonPath("$.balanceChange").value(balanceChange.intValue()))
                    .andReturn();

            sum.addAndGet(balanceChange.longValue());
            count.getAndIncrement();

          }

        Player player = playerRepository.findByUsername(PLAYER_USERNAME);
        String format = "sum(%s)-updateCount(%s)-transactions(%s)";

        String sumAndCountRepository = String.format(format,
                                                     player.getBalance(),
                                                     player.getVersion()-playerState.getVersion(),
                                                     playerTransactionRepository.findAll().size()-transactionCountState);
        String sumAndCountExpected = String.format(format,
                                                   playerState.getBalance().add(BigDecimal.valueOf(sum.get())),
                                                   count.get(),
                                                   count.get());

        assertEquals(sumAndCountExpected, sumAndCountRepository);

    }


}

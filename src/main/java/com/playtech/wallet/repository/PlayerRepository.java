package com.playtech.wallet.repository;

import com.playtech.wallet.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigInteger;

public interface PlayerRepository extends JpaRepository<Player, BigInteger>{

    /**
     * Search the player by username
     * @param username username of player
     * @return Player with account info
     */
    public Player findByUsername(String username);

}

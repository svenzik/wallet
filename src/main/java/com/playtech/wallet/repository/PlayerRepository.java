package com.playtech.wallet.repository;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;

import java.util.List;

/**
 * Persisting Player domain object
 */
public interface PlayerRepository {

    /**
     * Search the player by username
     * @param username username of player
     * @return Player with Balance info
     */
    Player findByUsername(String username) throws PlayerNotFoundException;

    /**
     * Persist data to database
     * @param player Player instance to persist
     * @return persisted player
     */
    Player save(Player player);

    /**
     * List all players
     * @return list of alla players
     */
    List<Player> findAll();

}

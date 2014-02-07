package com.playtech.wallet.repository;

import com.playtech.wallet.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerJpaRepository extends JpaRepository<Player, Long>{

    /**
     * Search the player by username
     * @param username username of player
     * @return Player with Balance info
     */
    public Player findByUsername(String username);

}

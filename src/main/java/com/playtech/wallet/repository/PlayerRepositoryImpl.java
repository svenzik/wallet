package com.playtech.wallet.repository;

import com.playtech.wallet.domain.Player;
import com.playtech.wallet.repository.exceptions.PlayerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PlayerRepositoryImpl implements PlayerRepository {

    @Autowired
    public PlayerRepositoryImpl(PlayerJpaRepository playerJpaRepository) {
        this.crud = playerJpaRepository;
    }

    private PlayerJpaRepository crud;

    @Override
    public Player findByUsername(String username) throws PlayerNotFoundException {
        Player p = crud.findByUsername(username);
        if (p == null) {
            throw new PlayerNotFoundException(username);
        }
        return p;
    }

    @Override
    public Player save(Player player) {
        return crud.save(player);
    }

    @Override
    public List<Player> findAll() {
        return crud.findAll();
    }
}

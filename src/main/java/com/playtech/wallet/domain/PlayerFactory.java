package com.playtech.wallet.domain;

public class PlayerFactory {

    public static Player createNewPlayer(String username) {
        Player player = new Player();
        player.setUsername(username);
        return player;
    }
}

package com.playtech.wallet.repository.exceptions;

/**
 * Exception thrown, when Player does not exist
 */
public class PlayerNotFoundException extends Exception {
    public PlayerNotFoundException(String username) {
        super("Player not found: " + username);
    }
}

package com.playtech.wallet.spring.controller;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.UUID;

@XmlRootElement
public class WalletChangeMessage {

    /**
     * So called authenticated username
     */
    private String username;

    /**
     * For shared systems a random transaction id is required (not sequential)
     */
    private UUID transactionId;

    /**
     * Balance change: debit (positive values), credit (negative values)
     */
    BigDecimal balanceChange;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getBalanceChange() {
        return balanceChange;
    }

    public void setBalanceChange(BigDecimal balanceChange) {
        this.balanceChange = balanceChange;
    }
}

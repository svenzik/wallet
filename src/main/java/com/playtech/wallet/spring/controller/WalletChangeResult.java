package com.playtech.wallet.spring.controller;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.UUID;


@XmlRootElement
public class WalletChangeResult {

    private UUID transactionId;

    /**
     * Since we are using errorCode, let it be String
     */
    private String errorCode;

    private Long balanceVersion;

    private BigDecimal balanceChange;

    private BigDecimal totalBalance;

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public Long getBalanceVersion() {
        return balanceVersion;
    }

    public void setBalanceVersion(Long balanceVersion) {
        this.balanceVersion = balanceVersion;
    }

    public BigDecimal getBalanceChange() {
        return balanceChange;
    }

    public void setBalanceChange(BigDecimal balanceChange) {
        this.balanceChange = balanceChange;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }
}

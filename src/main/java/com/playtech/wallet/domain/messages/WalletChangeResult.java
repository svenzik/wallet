package com.playtech.wallet.domain.messages;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.UUID;


/**
 * Outgoing message from controller
 */
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

    @Override
    public String toString() {
        return "WalletChangeResult{" +
                "transactionId=" + transactionId +
                ", errorCode='" + errorCode + '\'' +
                ", balanceVersion=" + balanceVersion +
                ", balanceChange=" + balanceChange +
                ", totalBalance=" + totalBalance +
                '}';
    }
}

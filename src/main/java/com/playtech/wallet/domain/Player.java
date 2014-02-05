package com.playtech.wallet.domain;

import com.playtech.wallet.domain.exceptions.BalanceLessThenZeroException;

import javax.persistence.*;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlRootElement;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Player domain object, not threadsafe!
 */
@XmlRootElement
@Entity
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQUENCE")
    @SequenceGenerator(name = "SEQUENCE", sequenceName = "PLAYER_ID_SEQ")
    private BigInteger id;

    @Column(length = 100, unique = true)
    private String username;

    @Min(0)
//    @Access(AccessType.PROPERTY)
    private BigDecimal balance = BigDecimal.ZERO;

    @Version
    @Column(name = "BALANCE_VERSION")
    private Long version;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (this.username == null) {
            this.username = username;
        }
    }

    public BigDecimal getBalance() {
        return balance;
    }

    /**
     * For use with JPA @Access(AccessType.PROPERTY)
     * @param balance Balance, if null then replaced with ZERO
     */
    private void setBalance(BigDecimal balance) {
        if (balance == null){
            balance = BigDecimal.ZERO;
        }
        this.balance = balance;
    }

    /**
     * Change the balance
     * @param delta debit (positive values), credit (negative values)
     */
    public void changeBalance(BigDecimal delta) {

        //compare result to ZERO
        if (getBalance().add(delta).compareTo(BigDecimal.ZERO) <= 0 ) {
            throw new BalanceLessThenZeroException(getBalance(), delta);
        }

        this.balance = this.getBalance().add(delta);

    }

    public Long getVersion() {
        return version;
    }
}

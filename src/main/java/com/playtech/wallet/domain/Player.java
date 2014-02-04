package com.playtech.wallet.domain;

import javax.persistence.*;
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

    private BigDecimal balance = BigDecimal.ZERO;

//    private AtomicReference<BigDecimal> amountAtomic = new AtomicReference<BigDecimal>(BigDecimal.ZERO);

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
//        return amountAtomic.get();
    }

    /**
     * Change the balance
     * @param delta debit (positive values), credit (negative values)
     */
    public void changeBalance(BigDecimal delta) {

        this.balance = this.balance.add(delta);
//        for (;;) {
//            BigDecimal oldVal = amountAtomic.get();
//            if (amountAtomic.compareAndSet(oldVal, oldVal.add(delta))) {
//                return;
//            }
//        }
    }

    public Long getVersion() {
        return version;
    }
}

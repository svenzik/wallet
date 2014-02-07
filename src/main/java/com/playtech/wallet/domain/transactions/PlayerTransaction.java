package com.playtech.wallet.domain.transactions;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.UUID;

@Entity
public class PlayerTransaction {

    protected PlayerTransaction() {
    }

    public PlayerTransaction(UUID transactionId) {
        this.transactionId = transactionId;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQUENCE")
    @SequenceGenerator(name = "SEQUENCE", sequenceName = "PLAYER_TRANSACTION_ID_SEQ")
    private Long id;

    /**
     * For shared systems a random transaction id is required (not sequential)
     */
    @Type(type = "org.hibernate.type.UUIDCharType")
    @Column(unique = true, updatable = false)
    private UUID transactionId;

    public UUID getTransactionId() {
        return transactionId;
    }
}

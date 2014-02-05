package com.playtech.wallet.domain;

import javax.persistence.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * Represents as result of collected statistics over time timeLengthSeconds
 */
@Entity
public class TimedMethodStatistics {

    //for JPA
    protected TimedMethodStatistics() {
    }

    public TimedMethodStatistics(String methodFullName, BigInteger timeLengthSeconds, BigDecimal minimumRequestTimeMs, BigDecimal maximumRequestTimeMs, BigDecimal averageRequestTimeMs) {
        this.methodFullName = methodFullName;
        this.timeLengthSeconds = timeLengthSeconds;
        this.minimumRequestTimeMs = minimumRequestTimeMs;
        this.maximumRequestTimeMs = maximumRequestTimeMs;
        this.averageRequestTimeMs = averageRequestTimeMs;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SEQUENCE")
    @SequenceGenerator(name = "SEQUENCE", sequenceName = "TimedMethodStatistics_ID_SEQ")
    private BigInteger id;

    private String methodFullName;
    private BigInteger timeLengthSeconds;

    private BigDecimal minimumRequestTimeMs;
    private BigDecimal maximumRequestTimeMs;
    private BigDecimal averageRequestTimeMs;

    private Date created = new Date();

}

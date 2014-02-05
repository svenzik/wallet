package com.playtech.wallet.domain.exceptions;

import java.math.BigDecimal;

public class BalanceLessThenZeroException extends RuntimeException {

    public BalanceLessThenZeroException(BigDecimal currentBalance, BigDecimal delta) {
        super(String.format("Cannot deduct more (%s) amount then balance: %s ", delta, currentBalance));
    }

}

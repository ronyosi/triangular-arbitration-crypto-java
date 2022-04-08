package com.webhopper.entities;

import java.math.BigDecimal;

public class RealArbCalcResult {
    private final RealArbCalcState state;
    private final BigDecimal amount;

    public RealArbCalcResult(RealArbCalcState state, BigDecimal amount) {
        this.state = state;
        this.amount = amount;
    }

    public RealArbCalcState getState() {
        return state;
    }

    public BigDecimal getAmountAcquired() {
        return amount;
    }

}


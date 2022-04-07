package com.webhopper.entities;

import java.math.BigDecimal;

public class TriArbTrade {
    final TriArbTradeLeg leg1;
    final TriArbTradeLeg leg2;
    final TriArbTradeLeg leg3;
    final BigDecimal profit;
    final BigDecimal profitPercent;


    public TriArbTrade(TriArbTradeLeg leg1, TriArbTradeLeg leg2, TriArbTradeLeg leg3, BigDecimal profit, BigDecimal profitPercent) {
        this.leg1 = leg1;
        this.leg2 = leg2;
        this.leg3 = leg3;
        this.profit = profit;
        this.profitPercent = profitPercent;
    }

    public TriArbTradeLeg getLeg1() {
        return leg1;
    }

    public TriArbTradeLeg getLeg2() {
        return leg2;
    }

    public TriArbTradeLeg getLeg3() {
        return leg3;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public BigDecimal getProfitPercent() {
        return profitPercent;
    }
}

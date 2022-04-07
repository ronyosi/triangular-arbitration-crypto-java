package com.webhopper.business;

import com.webhopper.entities.*;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PolonixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurfaceArbitrageCalculator {
    private static final Logger logger = LoggerFactory.getLogger(SurfaceArbitrageCalculator.class);

    private PolonixService poloniexService;

    public SurfaceArbitrageCalculator(PolonixService poloniexService) {
        this.poloniexService = poloniexService;

    }

    public List<TriArbTrade> calculateSurfaceArbitrage(
            final Triangle triangle,
            final Map<String, PairQuote> quotes,
            final BigDecimal startingAmount) {
        // a is always the first pair whether we go in forward or reverse.
        final Pair pairA = triangle.getA();
        final Pair pairB = triangle.getB();
        final Pair pairC = triangle.getC();

        final PairQuote pairAPricing = quotes.get(pairA.getPair());

        // Setup forward and reverse lists
        List<Pair> forward = new ArrayList<>();
        List<Pair> reverse = new ArrayList<>();

        forward.add(pairA);
        reverse.add(pairA);
        // however:
        // 1) for forward: we use pairA to trade from base to quote
        final String pairAQuote = pairA.getQuote();
        if(pairB.getBase().equals(pairAQuote) || pairB.getQuote().equals(pairAQuote)) {
            forward.add(pairB);
            forward.add(pairC); // since a and b were added, pairC must be the last pair in the triangle.
        } else if(pairC.getBase().equals(pairAQuote) || pairC.getQuote().equals(pairAQuote)) {
            forward.add(pairC);
            forward.add(pairB); // since a and c were added, pairC must be added last.
        }

        // 2) for reverse: we use pairA to trade from quote to base.
        final String pairABase = pairA.getBase();
        if(pairB.getBase().equals(pairABase) || pairB.getQuote().equals(pairABase)) {
            reverse.add(pairB);
            reverse.add(pairC); // since a and b were added, pairC must be the last pair in the triangle.
        } else if(pairC.getBase().equals(pairABase) || pairC.getQuote().equals(pairABase)) {
            reverse.add(pairC);
            reverse.add(pairB); // since a and c were added, pairC must be added last.
        }

        List<TriArbTradeLeg> forwardTriArbTrades = new ArrayList<>(3);
        // calculate forward:
        TriArbTradeLeg trade1Forward = new TriArbTradeLeg();
        trade1Forward.setPairTradeDirection(PairTradeDirection.BASE_TO_QUOTE);
        trade1Forward.setPair(pairA);
        trade1Forward.setAmountIn(startingAmount);
        trade1Forward.setCoinIn(pairA.getBase());
        trade1Forward.setCoinOut(pairA.getQuote());
        trade1Forward.setSwapRate(new BigDecimal(1.0).divide(pairAPricing.getAsk(),7, RoundingMode.HALF_UP));
        trade1Forward.setAmountOut(trade1Forward.getSwapRate().multiply(startingAmount));
        forwardTriArbTrades.add(trade1Forward);

        completeSurfaceCalculation(quotes, forward, forwardTriArbTrades);

        // calculate reverse:
        List<TriArbTradeLeg> reverseTriArbTrades = new ArrayList<>(3);

        TriArbTradeLeg trade1Reverse = new TriArbTradeLeg();
        trade1Reverse.setPairTradeDirection(PairTradeDirection.QUOTE_TO_BASE);
        trade1Reverse.setPair(pairA);
        trade1Reverse.setAmountIn(startingAmount);
        trade1Reverse.setCoinIn(pairA.getQuote());
        trade1Reverse.setCoinOut(pairA.getBase());
        trade1Reverse.setSwapRate(pairAPricing.getBid());
        trade1Reverse.setAmountOut(trade1Reverse.getSwapRate().multiply(startingAmount));
        reverseTriArbTrades.add(trade1Reverse);

        completeSurfaceCalculation(quotes, reverse, reverseTriArbTrades);

        final List<TriArbTrade> forwardAndReverseCalculations = new ArrayList<>();

        logger.info("FORWARD TRADES for {}", formatTradeText(forwardTriArbTrades));
        final TriArbTrade forwardTrade = calculateAndAddProfitability(startingAmount, forwardTriArbTrades);
        forwardAndReverseCalculations.add(forwardTrade);
        logger.info("REVERSE TRADES for {}", formatTradeText(reverseTriArbTrades));
        final TriArbTrade reverseTrade = calculateAndAddProfitability(startingAmount, reverseTriArbTrades);
        forwardAndReverseCalculations.add(reverseTrade);
        return forwardAndReverseCalculations;
    }

    private String formatTradeText(List<TriArbTradeLeg> trade) {
        final String leg1 = trade.get(0).getPair().getPair();
        final String leg2 = trade.get(1).getPair().getPair();
        final String leg3 = trade.get(2).getPair().getPair();
        return String.format("%s => %s => %s", leg1, leg2, leg3);
    }

    private TriArbTrade calculateAndAddProfitability(
            final BigDecimal amount,
            final List<TriArbTradeLeg> triArbTrades) {

        final TriArbTradeLeg tradeA = triArbTrades.get(0);
        final TriArbTradeLeg tradeB = triArbTrades.get(1);
        final TriArbTradeLeg endTrade = triArbTrades.get(2);
        final BigDecimal profit = endTrade.getAmountOut().subtract(amount);

        // Calculate profit %
        final BigDecimal divide = profit.divide(amount, 7, RoundingMode.HALF_UP);
        final BigDecimal profitPercentage = divide.multiply(new BigDecimal(100));

        return new TriArbTrade(tradeA, tradeB, endTrade, profit, profitPercentage);
    }

    public static void logSurfaceRateInfo(TriArbTrade fullTriArbTrade) {
//    public static void logSurfaceRateInfo(TriArbTradeLeg tradeA, TriArbTradeLeg tradeB, TriArbTradeLeg endTrade, BigDecimal profit, BigDecimal profitPercentage) {
        logger.info("====== Trade A: ======\n");
        logger.info(fullTriArbTrade.getLeg1().toString());
        logger.info("====== Trade B ======\n");

        logger.info(fullTriArbTrade.getLeg2().toString());
        logger.info("====== Trade C ======\n");
        logger.info(fullTriArbTrade.getLeg3().toString());

        logger.info("Profit: {} ", fullTriArbTrade.getProfit());
        logger.info("Profit Percentage: {}%", fullTriArbTrade.getProfitPercent());
    }

    private void completeSurfaceCalculation(Map<String, PairQuote> quotes, List<Pair> forward, List<TriArbTradeLeg> triArbTrades) {
        for(int i = 1; i < forward.size(); i++) {
            final TriArbTradeLeg previousTrade = triArbTrades.get(i-1);
            final Pair nextPair = forward.get(i);
            final PairQuote nextPairPricing = quotes.get(nextPair.getPair());

            final TriArbTradeLeg trade = new TriArbTradeLeg();
            trade.setPair(nextPair);
            trade.setAmountIn(previousTrade.getAmountOut());
            trade.setCoinIn(previousTrade.getCoinOut());

            if(previousTrade.getCoinOut().equals(nextPair.getBase())) {
                trade.setPairTradeDirection(PairTradeDirection.BASE_TO_QUOTE);
                trade.setCoinOut(nextPair.getQuote());
                trade.setSwapRate(new BigDecimal(1.0).divide(nextPairPricing.getAsk(),7, RoundingMode.HALF_UP));
            } else if(previousTrade.getCoinOut().equals(nextPair.getQuote())) {
                trade.setPairTradeDirection(PairTradeDirection.QUOTE_TO_BASE);
                trade.setCoinOut(nextPair.getBase());
                trade.setSwapRate(nextPairPricing.getBid());
            }

            trade.setAmountOut(trade.getSwapRate().multiply(previousTrade.getAmountOut()));
            triArbTrades.add(trade);
        }
    }
}

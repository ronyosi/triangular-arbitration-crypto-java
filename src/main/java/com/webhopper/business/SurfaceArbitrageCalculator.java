package com.webhopper.business;

import com.webhopper.entities.*;
import com.webhopper.integrations.ExchangeMarketDataService;
import com.webhopper.integrations.poloniex.PoloniexQuote;
import com.webhopper.integrations.poloniex.Quote;
import com.webhopper.integrations.poloniex.UniswapQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SurfaceArbitrageCalculator {
    private static final Logger logger = LoggerFactory.getLogger(SurfaceArbitrageCalculator.class);

    private ExchangeMarketDataService exchangeMarketDataService;

    public SurfaceArbitrageCalculator(ExchangeMarketDataService exchangeMarketDataService) {
        this.exchangeMarketDataService = exchangeMarketDataService;

    }

    public List<TriArbTrade> calculateSurfaceArbitrage(
            final Triangle triangle,
            final Map<String, Quote> quotes,
            final BigDecimal startingAmount) {
        // a is always the first pair whether we go in forward or reverse.
        final Pair pairA = triangle.getPairA();
        final Pair pairB = triangle.getPairB();
        final Pair pairC = triangle.getPairC();

        final Quote pairAPricing = quotes.get(pairA.getPair());

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
        trade1Forward.setSurfaceCalcAmountIn(startingAmount);
        trade1Forward.setCoinIn(pairA.getBase());
        trade1Forward.setCoinOut(pairA.getQuote());
        trade1Forward.setSurfaceCalcSwapRate(calculateBaseToQuote(pairAPricing));
        trade1Forward.setSurfaceCalcAmountOut(trade1Forward.getSwapRate().multiply(startingAmount));
        forwardTriArbTrades.add(trade1Forward);

        completeSurfaceCalculation(quotes, forward, forwardTriArbTrades);

        // calculate reverse:
        List<TriArbTradeLeg> reverseTriArbTrades = new ArrayList<>(3);

        TriArbTradeLeg trade1Reverse = new TriArbTradeLeg();
        trade1Reverse.setPairTradeDirection(PairTradeDirection.QUOTE_TO_BASE);
        trade1Reverse.setPair(pairA);
        trade1Reverse.setSurfaceCalcAmountIn(startingAmount);
        trade1Reverse.setCoinIn(pairA.getQuote());
        trade1Reverse.setCoinOut(pairA.getBase());
        trade1Reverse.setSurfaceCalcSwapRate(calculateQuoteToBase(pairAPricing));
        trade1Reverse.setSurfaceCalcAmountOut(trade1Reverse.getSwapRate().multiply(startingAmount));
        reverseTriArbTrades.add(trade1Reverse);

        completeSurfaceCalculation(quotes, reverse, reverseTriArbTrades);

        final List<TriArbTrade> forwardAndReverseCalculations = new ArrayList<>();

        logger.debug("FORWARD TRADES for {}", formatTradeText(forwardTriArbTrades));
        final TriArbTrade forwardTrade = calculateAndAddProfitability(startingAmount, forwardTriArbTrades);
        forwardAndReverseCalculations.add(forwardTrade);
        logger.debug("REVERSE TRADES for {}", formatTradeText(reverseTriArbTrades));
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

        final TriArbTradeLeg leg1 = triArbTrades.get(0);
        final TriArbTradeLeg leg2 = triArbTrades.get(1);
        final TriArbTradeLeg leg3 = triArbTrades.get(2);
        final BigDecimal profit = leg3.getSurfaceCalcAmountOut().subtract(amount);

        // Calculate profit %
        final BigDecimal divide = profit.divide(amount, 7, RoundingMode.HALF_UP);
        final BigDecimal profitPercentage = divide.multiply(new BigDecimal(100));

        return new TriArbTrade(leg1, leg2, leg3, profit, profitPercentage);
    }

    public static void logSurfaceRateInfo(TriArbTrade fullTriArbTrade) {
//    public static void logSurfaceRateInfo(TriArbTradeLeg tradeA, TriArbTradeLeg tradeB, TriArbTradeLeg endTrade, BigDecimal profit, BigDecimal profitPercentage) {
        logger.debug("====== Trade A: ======\n");
        logger.debug(fullTriArbTrade.getLeg1().toString());
        logger.debug("====== Trade B ======\n");

        logger.debug(fullTriArbTrade.getLeg2().toString());
        logger.debug("====== Trade C ======\n");
        logger.debug(fullTriArbTrade.getLeg3().toString());

        logger.debug("Profit: {} ", fullTriArbTrade.getSurfaceCalcProfit());
        logger.debug("Profit Percentage: {}%", fullTriArbTrade.getSurfaceCalcProfitPercent());
    }

    private void completeSurfaceCalculation(Map<String, Quote> quotes, List<Pair> forward, List<TriArbTradeLeg> triArbTrades) {
        for(int i = 1; i < forward.size(); i++) {
            final TriArbTradeLeg previousTrade = triArbTrades.get(i-1);
            final Pair nextPair = forward.get(i);
            final Quote nextPairPricing = quotes.get(nextPair.getPair());

            final TriArbTradeLeg trade = new TriArbTradeLeg();
            trade.setPair(nextPair);
            trade.setCoinIn(previousTrade.getCoinOut());
            trade.setSurfaceCalcAmountIn(previousTrade.getSurfaceCalcAmountOut());

            if(previousTrade.getCoinOut().equals(nextPair.getBase())) {
                trade.setPairTradeDirection(PairTradeDirection.BASE_TO_QUOTE);
                trade.setCoinOut(nextPair.getQuote());
                trade.setSurfaceCalcSwapRate(calculateBaseToQuote(nextPairPricing));
            } else if(previousTrade.getCoinOut().equals(nextPair.getQuote())) {
                trade.setPairTradeDirection(PairTradeDirection.QUOTE_TO_BASE);
                trade.setCoinOut(nextPair.getBase());
                trade.setSurfaceCalcSwapRate(calculateQuoteToBase(nextPairPricing));
            }

            trade.setSurfaceCalcAmountOut(trade.getSwapRate().multiply(previousTrade.getSurfaceCalcAmountOut()));
            triArbTrades.add(trade);
        }
    }

    private BigDecimal calculateQuoteToBase(Quote quote) {
        if(quote.getCryptoExchange() == CryptoExchange.POLONIEX) {
            return ((PoloniexQuote) quote).getBid();
        } else if(quote.getCryptoExchange() == CryptoExchange.UNISWAP) {
            return ((UniswapQuote) quote).getToken0Price();
        }

        return null;
    }

    private BigDecimal calculateBaseToQuote(Quote quote) {
        if(quote.getCryptoExchange() == CryptoExchange.POLONIEX) {
            return new BigDecimal(1.0).divide(((PoloniexQuote) quote).getAsk(), 7, RoundingMode.HALF_UP);
        } else if(quote.getCryptoExchange() == CryptoExchange.UNISWAP) {
            return ((UniswapQuote) quote).getToken1Price();
        }

        return null;
    }
}

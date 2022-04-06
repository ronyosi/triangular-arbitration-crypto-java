package com.webhopper.business;

import com.webhopper.entities.*;
import com.webhopper.poloniex.BookEntry;
import com.webhopper.poloniex.OrderBook;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PolonixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArbitrageCalculator {
    private static final Logger logger = LoggerFactory.getLogger(ArbitrageCalculator.class);

    private PolonixService poloniexService;

    public ArbitrageCalculator(PolonixService poloniexService) {
        this.poloniexService = poloniexService;

    }

    public List<FullTriArbTrade> calculateSurfaceArbitrage(
            final Triangle triangle,
            final Map<String, PairQuote> quotes,
            final BigDecimal amount) {
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
        trade1Forward.setAmountIn(amount);
        trade1Forward.setCoinIn(pairA.getBase());
        trade1Forward.setCoinOut(pairA.getQuote());
        trade1Forward.setSwapRate(new BigDecimal(1.0).divide(pairAPricing.getAsk(),7, RoundingMode.HALF_UP));
        trade1Forward.setAmountOut(trade1Forward.getSwapRate().multiply(amount));
        forwardTriArbTrades.add(trade1Forward);

        completeSurfaceCalculation(quotes, forward, forwardTriArbTrades);

        // calculate reverse:
        List<TriArbTradeLeg> reverseTriArbTrades = new ArrayList<>(3);

        TriArbTradeLeg trade1Reverse = new TriArbTradeLeg();
        trade1Reverse.setPairTradeDirection(PairTradeDirection.QUOTE_TO_BASE);
        trade1Reverse.setPair(pairA);
        trade1Reverse.setAmountIn(amount);
        trade1Reverse.setCoinIn(pairA.getQuote());
        trade1Reverse.setCoinOut(pairA.getBase());
        trade1Reverse.setSwapRate(pairAPricing.getBid());
        trade1Reverse.setAmountOut(trade1Reverse.getSwapRate().multiply(amount));
        reverseTriArbTrades.add(trade1Reverse);

        completeSurfaceCalculation(quotes, reverse, reverseTriArbTrades);

        final List<FullTriArbTrade> forwardAndReverseCalculations = new ArrayList<>();

        logger.info("FORWARD TRADES for {}", formatTradeText(forwardTriArbTrades));
        final FullTriArbTrade forwardTrade = calculateAndAddProfitability(amount, forwardTriArbTrades);
        forwardAndReverseCalculations.add(forwardTrade);
        logger.info("REVERSE TRADES for {}", formatTradeText(reverseTriArbTrades));
        final FullTriArbTrade reverseTrade = calculateAndAddProfitability(amount, reverseTriArbTrades);
        forwardAndReverseCalculations.add(reverseTrade);
        return forwardAndReverseCalculations;
    }

    private String formatTradeText(List<TriArbTradeLeg> trade) {
        final String leg1 = trade.get(0).getPair().getPair();
        final String leg2 = trade.get(1).getPair().getPair();
        final String leg3 = trade.get(2).getPair().getPair();
        return String.format("%s => %s => %s", leg1, leg2, leg3);
    }

    private FullTriArbTrade calculateAndAddProfitability(
            final BigDecimal amount,
            final List<TriArbTradeLeg> triArbTrades) {

        final TriArbTradeLeg tradeA = triArbTrades.get(0);
        final TriArbTradeLeg tradeB = triArbTrades.get(1);
        final TriArbTradeLeg endTrade = triArbTrades.get(2);
        final BigDecimal profit = endTrade.getAmountOut().subtract(amount);

        // Calculate profit %
        final BigDecimal divide = profit.divide(amount, 7, RoundingMode.HALF_UP);
        final BigDecimal profitPercentage = divide.multiply(new BigDecimal(100));

        return new FullTriArbTrade(tradeA, tradeB, endTrade, profit, profitPercentage);
    }

    public static void logSurfaceRateInfo(FullTriArbTrade fullTriArbTrade) {
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

   public Map<String, Object> calculateDepthArbitrage(final FullTriArbTrade triangle) {
       final TriArbTradeLeg leg1 = triangle.getLeg1();
       final String pairA = leg1.getPair().getPair();
       final TriArbTradeLeg leg2 = triangle.getLeg2();
       final String pairB = leg2.getPair().getPair();
       final TriArbTradeLeg leg3 = triangle.getLeg3();
       final String pairC = leg3.getPair().getPair();

       final OrderBook bookForPairA = poloniexService.getBookForPair(pairA);
       final OrderBook bookForPairB = poloniexService.getBookForPair(pairB);
       final OrderBook bookForPairC = poloniexService.getBookForPair(pairC);

       final List<BookEntry> repriceForLeg1Calculation = reformatOrderbook(bookForPairA, leg1.getPairTradeDirection());
       final List<BookEntry> repriceForLeg2Calculation = reformatOrderbook(bookForPairB, leg2.getPairTradeDirection());
       final List<BookEntry> repriceForLeg3Calculation = reformatOrderbook(bookForPairC, leg3.getPairTradeDirection());

       final BigDecimal startingAmount = leg1.getAmountIn();
       final BigDecimal aquiredCoinLeg1 = calculateDeepProfitablity(startingAmount, repriceForLeg1Calculation);
       final BigDecimal aquiredCoinLeg2 = calculateDeepProfitablity(aquiredCoinLeg1, repriceForLeg2Calculation);
       final BigDecimal aquiredCoinLeg3 = calculateDeepProfitablity(aquiredCoinLeg2, repriceForLeg3Calculation);

    // Calculate Profit Loss Also Known As Real Rate
    BigDecimal profitLoss = aquiredCoinLeg3.subtract(startingAmount);
    double realRatePercent  = (profitLoss.doubleValue() / startingAmount.doubleValue()) * 100;

    if (realRatePercent > -1) {
        Map<String, Object> realRateInfo = new HashMap<>();
        realRateInfo.put("profit_loss", profitLoss);
        realRateInfo.put("real_rate_percent", realRatePercent);
        realRateInfo.put("real_rate_percent", realRatePercent);
        realRateInfo.put("leg_1", leg1);
        realRateInfo.put("leg_2", leg2);
        realRateInfo.put("leg_3", leg3);
        realRateInfo.put("leg_1_direction", leg1.getPairTradeDirection());
        realRateInfo.put("leg_2_direction", leg2.getPairTradeDirection());
        realRateInfo.put("leg_3_direction", leg3.getPairTradeDirection());

        return realRateInfo;
    } else {
        return null;
    }

   }

   private BigDecimal calculateDeepProfitablity(BigDecimal amountIn, List<BookEntry> orderBook) {
       BigDecimal tradingBalance = amountIn;
       BigDecimal quantityBought = new BigDecimal(0);
       BigDecimal acquiredCoin = new BigDecimal(0);
       int counts = 0;

       for(BookEntry bookEntry : orderBook) {
           final BigDecimal levelPrice = bookEntry.getPrice();
           final int levelAvailableQuantity = bookEntry.getQuantity();

           BigDecimal amountBought = new BigDecimal(0);
           if (tradingBalance.doubleValue() <= levelAvailableQuantity) {
               quantityBought = tradingBalance;
               tradingBalance = new BigDecimal(0);
               amountBought = quantityBought.multiply(levelPrice);

           } else if (tradingBalance.doubleValue() > levelAvailableQuantity) {
               acquiredCoin = new BigDecimal(levelAvailableQuantity);
               tradingBalance = tradingBalance.subtract(quantityBought);
               amountBought = quantityBought.multiply(levelPrice);
           }

           acquiredCoin = acquiredCoin.add(amountBought);

           if(tradingBalance.doubleValue() == 0) {
               break;
           }

           counts += 1;
           if (counts == orderBook.size()) {
               acquiredCoin =  new BigDecimal(0);
               break;
           }
       }

       return acquiredCoin;
   }

   private  List<BookEntry> reformatOrderbook(OrderBook orderBook, PairTradeDirection direction) {
       final List<BookEntry> bookEntry = new ArrayList<>();

       if(direction == PairTradeDirection.BASE_TO_QUOTE) {
           for(BookEntry ask : orderBook.getAsks()) {
               BigDecimal recalculatedPrice = new BigDecimal(1).divide(ask.getPrice(), 7, RoundingMode.HALF_UP);
               bookEntry.add(new BookEntry(recalculatedPrice, ask.getQuantity()));
           }
       } else if(direction == PairTradeDirection.QUOTE_TO_BASE) {
           for(BookEntry ask : orderBook.getBids()) {
            BigDecimal recalculatedPrice = ask.getPrice().multiply(ask.getPrice());
            bookEntry.add(new BookEntry(recalculatedPrice, ask.getQuantity()));
           }
       }

       return bookEntry;
   }
}

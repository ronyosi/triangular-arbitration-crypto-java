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

public class RealArbitrageCalculator {
    private static final Logger logger = LoggerFactory.getLogger(RealArbitrageCalculator.class);

    private PolonixService poloniexService;

    public RealArbitrageCalculator(PolonixService poloniexService) {
        this.poloniexService = poloniexService;

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

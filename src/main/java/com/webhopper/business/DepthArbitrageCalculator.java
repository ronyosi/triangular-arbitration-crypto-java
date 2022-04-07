package com.webhopper.business;

import com.webhopper.entities.*;
import com.webhopper.poloniex.BookEntry;
import com.webhopper.poloniex.OrderBook;
import com.webhopper.poloniex.PolonixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepthArbitrageCalculator {
    private static final Logger logger = LoggerFactory.getLogger(DepthArbitrageCalculator.class);

    private PolonixService poloniexService;

    public DepthArbitrageCalculator(PolonixService poloniexService) {
        this.poloniexService = poloniexService;

    }

   public TriArbTrade calculateDepthArbitrage(final TriArbTrade triArbTrade) {
       final TriArbTradeLeg leg1 = triArbTrade.getLeg1();
       final String pairA = leg1.getPair().getPair();
       final TriArbTradeLeg leg2 = triArbTrade.getLeg2();
       final String pairB = leg2.getPair().getPair();
       final TriArbTradeLeg leg3 = triArbTrade.getLeg3();
       final String pairC = leg3.getPair().getPair();

       final OrderBook bookForPairA = poloniexService.getBookForPair(pairA);
       final OrderBook bookForPairB = poloniexService.getBookForPair(pairB);
       final OrderBook bookForPairC = poloniexService.getBookForPair(pairC);

       final List<BookEntry> repriceForLeg1Calculation = reformatOrderbook(bookForPairA, leg1.getPairTradeDirection());
       final List<BookEntry> repriceForLeg2Calculation = reformatOrderbook(bookForPairB, leg2.getPairTradeDirection());
       final List<BookEntry> repriceForLeg3Calculation = reformatOrderbook(bookForPairC, leg3.getPairTradeDirection());

       final BigDecimal startingAmount = leg1.getSurfaceCalcAmountIn();

       final BigDecimal aquiredCoinLeg1 = calculateDeepProfitablity(startingAmount, repriceForLeg1Calculation);
       leg1.setDepthCalcAmountOut(aquiredCoinLeg1);
       leg2.setDepthCalcAmountIn(aquiredCoinLeg1);
       final BigDecimal aquiredCoinLeg2 = calculateDeepProfitablity(aquiredCoinLeg1, repriceForLeg2Calculation);

       leg2.setDepthCalcAmountOut(aquiredCoinLeg2);
       leg3.setDepthCalcAmountIn(aquiredCoinLeg2);
       final BigDecimal aquiredCoinLeg3 = calculateDeepProfitablity(aquiredCoinLeg2, repriceForLeg3Calculation);
       leg3.setDepthCalcAmountOut(aquiredCoinLeg3);

    // Calculate Profit Loss Also Known As Real Rate
    final BigDecimal profitLoss = aquiredCoinLeg3.subtract(startingAmount);
    // Calculate profit %
    final BigDecimal divide = profitLoss.divide(startingAmount, 7, RoundingMode.HALF_UP);
    final BigDecimal profitPercentage = divide.multiply(new BigDecimal(100));

    triArbTrade.setDepthCalcProfit(profitLoss);
    triArbTrade.setDepthCalcProfitPercent(profitPercentage);

    return triArbTrade;
   }

   private BigDecimal calculateDeepProfitablity(BigDecimal amountIn, List<BookEntry> orderBook) {
       BigDecimal tradingBalance = amountIn;
       BigDecimal quantityBought = new BigDecimal(0);
       BigDecimal acquiredCoin = new BigDecimal(0);
       int counts = 0;

       for(BookEntry bookEntry : orderBook) {
           final BigDecimal levelPrice = bookEntry.getPrice();
           final BigDecimal levelAvailableQuantity = bookEntry.getQuantity();

           BigDecimal amountBought = new BigDecimal(0);
           if (tradingBalance.doubleValue() <= levelAvailableQuantity.doubleValue()) {
               quantityBought = tradingBalance;
               tradingBalance = new BigDecimal(0);
               amountBought = quantityBought.multiply(levelPrice);

           } else if (tradingBalance.doubleValue() > levelAvailableQuantity.doubleValue()) {
               quantityBought = levelAvailableQuantity;
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

   /*
   def reformated_orderbook(prices, c_direction):
    price_list_main = []
    if c_direction == "base_to_quote":
        for p in prices["asks"]:
            ask_price = float(p[0])
            adj_price = 1 / ask_price if ask_price != 0 else 0
            adj_quantity = float(p[1]) * ask_price
            price_list_main.append([adj_price, adj_quantity])
    if c_direction == "quote_to_base":
        for p in prices["bids"]:
            bid_price = float(p[0])
            adj_price = bid_price if bid_price != 0 else 0
            adj_quantity = float(p[1])
            price_list_main.append([adj_price, adj_quantity])
    return price_list_main
    */
   private  List<BookEntry> reformatOrderbook(OrderBook orderBook, PairTradeDirection direction) {
       final List<BookEntry> bookEntry = new ArrayList<>();

       if(direction == PairTradeDirection.BASE_TO_QUOTE) {
           for(BookEntry ask : orderBook.getAsks()) {
               BigDecimal adjustedPrice = new BigDecimal(0);
               BigDecimal adjustedQuantity = ask.getQuantity().multiply(ask.getPrice());
               // If divide by zero issue will not happen, run the math below.
               if(!ask.getPrice().equals(new BigDecimal(0))) {
                   adjustedPrice = new BigDecimal(1).divide(ask.getPrice(), 7, RoundingMode.HALF_UP);
               }

               bookEntry.add(new BookEntry(adjustedPrice, adjustedQuantity));
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



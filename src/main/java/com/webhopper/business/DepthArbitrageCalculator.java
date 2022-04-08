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
import java.util.List;

public class DepthArbitrageCalculator {
    private static final Logger logger = LoggerFactory.getLogger(DepthArbitrageCalculator.class);

    private PolonixService poloniexService;

    public DepthArbitrageCalculator(PolonixService poloniexService) {
        this.poloniexService = poloniexService;

    }

    public TriArbTrade calculateDepthArbitrage(final TriArbTrade triArbTrade) {
        final TriArbTradeLeg leg1 = triArbTrade.getLeg1();
        final String leg1Pair = leg1.getPair().getPair();
        final TriArbTradeLeg leg2 = triArbTrade.getLeg2();
        final String leg2Pair = leg2.getPair().getPair();
        final TriArbTradeLeg leg3 = triArbTrade.getLeg3();
        final String leg3Pair = leg3.getPair().getPair();

        final OrderBook bookForleg1Pair = poloniexService.getBookForPair(leg1Pair);
        final OrderBook bookForLeg2Pair = poloniexService.getBookForPair(leg2Pair);
        final OrderBook bookForLeg3Pair = poloniexService.getBookForPair(leg3Pair);

        //todo: write tests on the book repricing algo for sanity check.
        final List<BookEntry> repriceForLeg1Calculation = reformatOrderbook(bookForleg1Pair, leg1.getPairTradeDirection());
        final List<BookEntry> repriceForLeg2Calculation = reformatOrderbook(bookForLeg2Pair, leg2.getPairTradeDirection());
        final List<BookEntry> repriceForLeg3Calculation = reformatOrderbook(bookForLeg3Pair, leg3.getPairTradeDirection());

        final BigDecimal startingAmount = leg1.getSurfaceCalcAmountIn();
        leg1.setDepthCalcAmountIn(startingAmount);

        // Calculate leg1
        final RealArbCalcResult leg1Result = calculateDeepProfitablity(startingAmount, repriceForLeg1Calculation);
        if(leg1Result.getState() == RealArbCalcState.NOT_ENOUGH_BOOK_DEPTH) {
            triArbTrade.setDepthCalcState(DepthCalcState.NOT_ENOUGH_BOOK_DEPTH);
            return triArbTrade;
        }
        leg1.setDepthCalcAmountOut(leg1Result.getAmountAcquired());
        leg2.setDepthCalcAmountIn(leg1Result.getAmountAcquired());

        // Calculate leg2
        final RealArbCalcResult leg2Result = calculateDeepProfitablity(leg1Result.getAmountAcquired(), repriceForLeg2Calculation);
        if(leg2Result.getState() == RealArbCalcState.NOT_ENOUGH_BOOK_DEPTH) {
            triArbTrade.setDepthCalcState(DepthCalcState.NOT_ENOUGH_BOOK_DEPTH);
            return triArbTrade;
        }
        leg2.setDepthCalcAmountOut(leg2Result.getAmountAcquired());
        leg3.setDepthCalcAmountIn(leg2Result.getAmountAcquired());

        // Calculate leg3
        final RealArbCalcResult leg3Result = calculateDeepProfitablity(leg2Result.getAmountAcquired(), repriceForLeg3Calculation);
        if(leg3Result.getState() == RealArbCalcState.NOT_ENOUGH_BOOK_DEPTH) {
            triArbTrade.setDepthCalcState(DepthCalcState.NOT_ENOUGH_BOOK_DEPTH);
            return triArbTrade;
        }
        final BigDecimal leg3AmountAcquired = leg3Result.getAmountAcquired();
        leg3.setDepthCalcAmountOut(leg3AmountAcquired);

        // Calculate Profit Loss Also Known As Real Rate
        final BigDecimal profitLoss = leg3AmountAcquired.subtract(startingAmount);
        // Calculate profit %
        final BigDecimal divide = profitLoss.divide(startingAmount, 7, RoundingMode.HALF_UP);
        final BigDecimal profitPercentage = divide.multiply(new BigDecimal(100));

        triArbTrade.setDepthCalcProfit(profitLoss);
        triArbTrade.setDepthCalcProfitPercent(profitPercentage);

        triArbTrade.setDepthCalcState(DepthCalcState.SUCCESSFULLY_CALCULATED);
        return triArbTrade;
    }

    private RealArbCalcResult calculateDeepProfitablity(BigDecimal amountIn, List<BookEntry> orderBook) {
        BigDecimal tradingBalance = amountIn;
        BigDecimal quantityBought = new BigDecimal(0);
        BigDecimal acquiredCoin = new BigDecimal(0);
        int counts = 0;

        for (BookEntry bookEntry : orderBook) {
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

            if (tradingBalance.doubleValue() == 0) {
                break;
            }

            counts += 1;
            if (counts == orderBook.size()) {
               return new RealArbCalcResult(RealArbCalcState.NOT_ENOUGH_BOOK_DEPTH, new BigDecimal(0));
            }
        }

        return new RealArbCalcResult(RealArbCalcState.TRADING_BALANCE_SPENT, acquiredCoin);
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
    private List<BookEntry> reformatOrderbook(OrderBook orderBook, PairTradeDirection direction) {
        final List<BookEntry> bookEntry = new ArrayList<>();

        if (direction == PairTradeDirection.BASE_TO_QUOTE) {
            for (BookEntry ask : orderBook.getAsks()) {
                final BigDecimal askPrice = ask.getPrice();
                BigDecimal adjustedPrice = new BigDecimal(0);
                // If divide by zero issue will not happen, run the math below.
                if (!askPrice.equals(new BigDecimal(0))) {
                    adjustedPrice = new BigDecimal(1).divide(askPrice, 7, RoundingMode.HALF_UP);
                }
                BigDecimal adjustedQuantity = ask.getQuantity().multiply(askPrice);
                bookEntry.add(new BookEntry(adjustedPrice, adjustedQuantity));
            }
        } else if (direction == PairTradeDirection.QUOTE_TO_BASE) {
            for (BookEntry bid : orderBook.getBids()) {
                bookEntry.add(new BookEntry(bid.getPrice(), bid.getQuantity()));
            }
        }

        return bookEntry;
    }

    

}



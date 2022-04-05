package com.webhopper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.entities.*;
import com.webhopper.poloniex.OrderBook;
import com.webhopper.poloniex.PairQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ArbitrageCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ArbitrageCalculator.class);

//    final Gson gson = new Gson(); // Or use new GsonBuilder().create();
    /*

# Reformat Order Book for Depth Calculation
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

   private OrderBook getBookForPair(String pair) {
       var url = String.format("https://poloniex.com/public?command=returnOrderBook&currencyPair=%s&depth=20", pair);

       var client = HttpClient.newHttpClient();
       var request = HttpRequest.newBuilder(
               URI.create(url))
               .header("accept", "application/json")
               .build();

       HttpResponse<String> response = null;
       try {
           response = client.send(request, HttpResponse.BodyHandlers.ofString());
       } catch (IOException e) {
           e.printStackTrace();
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       ObjectMapper objectMapper = JsonFacade.getObjectMapper();

       OrderBook orderBook = null; // deserializes json into target2
       try {
           orderBook = objectMapper.readValue(response.body(), OrderBook.class);
       } catch (JsonProcessingException e) {
           e.printStackTrace();
       }

       return orderBook;
   }

    public List<FullTriArbTrade> calculateSurfaceArbitrage(
            final Triangle triangle,
            final Map<String, PairQuote> quotes,
            final BigDecimal amount,
            final BigDecimal percentProfitExpected) {
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
        trade1Forward.setSwapRate(new BigDecimal(1.0).divide(pairAPricing.getAsk(),14, RoundingMode.HALF_UP));
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

        final List<FullTriArbTrade> profitableTrades = new ArrayList<>();

        logger.info("FORWARD TRADES for {}", formatTradeText(forwardTriArbTrades));
        calculateProfitability(amount, percentProfitExpected, forwardTriArbTrades, profitableTrades);
        logger.info("REVERSE TRADES for {}", formatTradeText(reverseTriArbTrades));
        calculateProfitability(amount, percentProfitExpected, reverseTriArbTrades, profitableTrades);
        return profitableTrades;
    }

    private String formatTradeText(List<TriArbTradeLeg> trade) {
        final String leg1 = trade.get(0).getPair().getPair();
        final String leg2 = trade.get(1).getPair().getPair();
        final String leg3 = trade.get(2).getPair().getPair();
        return String.format("%s => %s => %s", leg1, leg2, leg3);
    }

    private void calculateProfitability(
            final BigDecimal amount,
            final BigDecimal percentProfitExpected,
            final List<TriArbTradeLeg> triArbTrades,
            final List<FullTriArbTrade> profitableTrades) {

        final TriArbTradeLeg tradeA = triArbTrades.get(0);
        final TriArbTradeLeg tradeB = triArbTrades.get(1);
        final TriArbTradeLeg endTrade = triArbTrades.get(2);
        final BigDecimal profit = endTrade.getAmountOut().subtract(amount);

        // Calculate profit %
        final BigDecimal divide = profit.divide(amount, 7, RoundingMode.HALF_UP);
        final BigDecimal profitPercentage = divide.multiply(new BigDecimal(100));

        final FullTriArbTrade fullTriArbTrade = new FullTriArbTrade(tradeA, tradeB, endTrade, profit, profitPercentage);
//        logSurfaceRateInfo(fullTriArbTrade);
        if(profitPercentage.compareTo(percentProfitExpected) > 0) {
            logger.info("This is potentially Profitable!");
            profitableTrades.add(fullTriArbTrade);
        }
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
                trade.setSwapRate(new BigDecimal(1.0).divide(nextPairPricing.getAsk(),14, RoundingMode.HALF_UP));
            } else if(previousTrade.getCoinOut().equals(nextPair.getQuote())) {
                trade.setPairTradeDirection(PairTradeDirection.QUOTE_TO_BASE);
                trade.setCoinOut(nextPair.getBase());
                trade.setSwapRate(nextPairPricing.getBid());
            }

            trade.setAmountOut(trade.getSwapRate().multiply(previousTrade.getAmountOut()));
            triArbTrades.add(trade);
        }
    }




   public void calculateDepthArbitrage(final Triangle triangle) {
       final OrderBook bookForPairA = getBookForPair(triangle.getPairA());
       final OrderBook bookForPairB = getBookForPair(triangle.getPairB());
       final OrderBook bookForPairC = getBookForPair(triangle.getPairC());

       // Forward direction calculation


   }



}

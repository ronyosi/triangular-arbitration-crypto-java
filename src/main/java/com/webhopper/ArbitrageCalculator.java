package com.webhopper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.poloniex.OrderBook;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PolonixApiFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
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

    public void calculateSurfaceArbitrage(final Triangle triangle, Map<String, PairQuote> quotes) {
        // a is always the first pair whether we go in forward or reverse.
        final Triangle.Pair pairA = triangle.a;
        final Triangle.Pair pairB = triangle.b;
        final Triangle.Pair pairC = triangle.c;

        final PairQuote pairAPricing = quotes.get(pairA.pair);

        double amount = 100;

        // Setup forward and reverse lists
        List<Triangle.Pair> forward = new ArrayList<>();
        List<Triangle.Pair> reverse = new ArrayList<>();


        forward.add(pairA);
        reverse.add(pairA);
        // however:
        // 1) for forward: we use pairA to trade from base to quote
        final String pairAQuote = pairA.quote;
        if(pairB.base.equals(pairAQuote) || pairB.quote.equals(pairAQuote)) {
            forward.add(pairB);
            forward.add(pairC); // since a and b were added, pairC must be the last pair in the triangle.
        } else if(pairC.base.equals(pairAQuote) || pairC.quote.equals(pairAQuote)) {
            forward.add(pairC);
            forward.add(pairB); // since a and c were added, pairC must be added last.
        }

        // 2) for reverse: we use pairA to trade from quote to base.
        final String pairABase = pairA.base;
        if(pairB.base.equals(pairABase) || pairB.quote.equals(pairABase)) {
            reverse.add(pairB);
            reverse.add(pairC); // since a and b were added, pairC must be the last pair in the triangle.
        } else if(pairC.base.equals(pairABase) || pairC.quote.equals(pairABase)) {
            reverse.add(pairC);
            reverse.add(pairB); // since a and c were added, pairC must be added last.
        }

        List<TriArbTrade> forwardTriArbTrades = new ArrayList<>(3);
        // calculate forward:
        TriArbTrade trade1Forward = new TriArbTrade();
        trade1Forward.setPairTradeDirection(PairDirection.BASE_TO_QUOTE);
        trade1Forward.setPair(pairA);
        trade1Forward.setAmountIn(amount);
        trade1Forward.setCoinIn(pairA.base);
        trade1Forward.setCoinOut(pairA.quote);
        trade1Forward.setCalculatedRate(pairAPricing.getAsk()/1);
        trade1Forward.setAmountOut(trade1Forward.getCalculatedRate() * amount);
        forwardTriArbTrades.add(trade1Forward);

        completeSurfaceCalculation(quotes, forward, forwardTriArbTrades);

        // calculate reverse:
        List<TriArbTrade> reverseTriArbTrades = new ArrayList<>(3);

        TriArbTrade trade1Reverse = new TriArbTrade();
        trade1Reverse.setPairTradeDirection(PairDirection.QUOTE_TO_BASE);
        trade1Reverse.setPair(pairA);
        trade1Reverse.setAmountIn(amount);
        trade1Reverse.setCoinIn(pairA.quote);
        trade1Reverse.setCoinOut(pairA.base);
        trade1Reverse.setCalculatedRate(pairAPricing.getBid());
        trade1Reverse.setAmountOut(trade1Reverse.getCalculatedRate() * amount);
        reverseTriArbTrades.add(trade1Reverse);

        completeSurfaceCalculation(quotes, reverse, reverseTriArbTrades);

        final TriArbTrade forwardEndResult = forwardTriArbTrades.get(2);
        logger.info("Forward Profit: {} ", forwardEndResult.getAmountOut() - amount);

        final TriArbTrade reverseEndResult = reverseTriArbTrades.get(2);
        logger.info("Reverse Profit: {} ", reverseEndResult.getAmountOut() - amount);


        System.out.println();





        //        """  FORWARD """
        //# SCENARIO 1 Check if a_quote (acquired_coin) matches b_quote
        //# SCENARIO 2 Check if a_quote (acquired_coin) matches b_base
        //# SCENARIO 3 Check if a_quote (acquired_coin) matches c_quote
        //# SCENARIO 4 Check if a_quote (acquired_coin) matches c_base
        //"""  REVERSE """
        //# SCENARIO 1 Check if a_base (acquired_coin) matches b_quote
        //# SCENARIO 2 Check if a_base (acquired_coin) matches b_base
        //# SCENARIO 3 Check if a_base (acquired_coin) matches c_quote
        //# SCENARIO 4 Check if a_base (acquired_coin) matches c_base
    }

    private void completeSurfaceCalculation(Map<String, PairQuote> quotes, List<Triangle.Pair> forward, List<TriArbTrade> triArbTrades) {
        for(int i = 1; i < forward.size(); i++) {
            final TriArbTrade previousTrade = triArbTrades.get(i-1);
            final Triangle.Pair nextPair = forward.get(i);
            final PairQuote nextPairPricing = quotes.get(nextPair.pair);

            final TriArbTrade trade = new TriArbTrade();
            trade.setPair(nextPair);
            trade.setAmountIn(previousTrade.amountOut);
            trade.setCoinIn(previousTrade.coinOut);

            if(previousTrade.getCoinOut().equals(nextPair.base)) {
                trade.setPairTradeDirection(PairDirection.BASE_TO_QUOTE);
                trade.setCoinOut(nextPair.quote);
                trade.setCalculatedRate(nextPairPricing.getAsk()/1);
            } else if(previousTrade.getCoinOut().equals(nextPair.quote)) {
                trade.setPairTradeDirection(PairDirection.QUOTE_TO_BASE);
                trade.setCoinOut(nextPair.base);
                trade.setCalculatedRate(nextPairPricing.getBid());
            }

            trade.setAmountOut(trade.getCalculatedRate() * previousTrade.getAmountOut());
            triArbTrades.add(trade);
        }
    }

    enum PairDirection {
        BASE_TO_QUOTE,
        QUOTE_TO_BASE;
    }

    class TriArbTrade {
        private Double amountIn;
        private Triangle.Pair pair;
        private PairDirection pairTradeDirection;
        private Double calculatedRate;
        private Double amountOut;
        private String coinIn;
        private String coinOut;

        public Double getAmountIn() {
            return amountIn;
        }

        public void setAmountIn(Double amountIn) {
            this.amountIn = amountIn;
        }

        public Triangle.Pair getPair() {
            return pair;
        }

        public void setPair(Triangle.Pair pair) {
            this.pair = pair;
        }

        public PairDirection getPairTradeDirection() {
            return pairTradeDirection;
        }

        public void setPairTradeDirection(PairDirection pairTradeDirection) {
            this.pairTradeDirection = pairTradeDirection;
        }

        public Double getCalculatedRate() {
            return calculatedRate;
        }

        public void setCalculatedRate(Double calculatedRate) {
            this.calculatedRate = calculatedRate;
        }

        public Double getAmountOut() {
            return amountOut;
        }

        public void setAmountOut(Double amountOut) {
            this.amountOut = amountOut;
        }

        public String getCoinIn() {
            return coinIn;
        }

        public void setCoinIn(String coinIn) {
            this.coinIn = coinIn;
        }

        public String getCoinOut() {
            return coinOut;
        }

        public void setCoinOut(String coinOut) {
            this.coinOut = coinOut;
        }
    }

   public void calculateDepthArbitrage(final Triangle triangle) {
       final OrderBook bookForPairA = getBookForPair(triangle.getPairA());
       final OrderBook bookForPairB = getBookForPair(triangle.getPairB());
       final OrderBook bookForPairC = getBookForPair(triangle.getPairC());

       // Forward direction calculation


   }



}

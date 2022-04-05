package com.webhopper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.poloniex.OrderBook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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

   public void calculateSurfaceArbitrage(final Triangle triangle) {
       OrderBook bookForPair = getBookForPair(triangle.getPairA());

   }



}

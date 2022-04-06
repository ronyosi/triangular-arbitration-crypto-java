package com.webhopper.poloniex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.utils.JsonFacade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PolonixService {
    final PoloniexApi poloniexApi;

    public PolonixService(PoloniexApi poloniexApi) {
        this.poloniexApi = poloniexApi;
    }

    public Map<String, PairQuote> getPricingInfo() {
        final String json = poloniexApi.getPricesFromFileOrApiCall(false);
        return mapPoloniexJsonToPairQuotes(json);
    }

    public OrderBook getBookForPair(String pair) {
        String json = poloniexApi.httpGetOrderBookForPair(pair);
        return  mapPoloniexBookJson(json);

    }

    private OrderBook mapPoloniexBookJson(String json) {
        final ObjectMapper objectMapper = JsonFacade.getObjectMapper();

        OrderBook orderBook = null; // deserializes json into target2
        try {
            orderBook = objectMapper.readValue(json, OrderBook.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return orderBook;
    }

    private static Map<String, PairQuote> mapPoloniexJsonToPairQuotes(String prices) {

        ObjectMapper objectMapper = JsonFacade.getObjectMapper();
        JsonNode priceData = null;
        try {
            priceData = objectMapper.readValue(prices, JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, PairQuote> pairQuotes = new HashMap<>();

        final Iterator<String> pairNames = priceData.fieldNames();

        while (pairNames.hasNext()) {
            final String pair = pairNames.next();
            JsonNode jsonNode = priceData.get(pair);

            if(jsonNode.get("isFrozen").asInt() == 1 || jsonNode.get("postOnly").asInt() == 1) {
                continue; //skip untradable coins.
            }

            String[] split = pair.split("_");
            String base = split[0];
            String quote = split[1];

            double ask = jsonNode.get("lowestAsk").asDouble();
            double bid = jsonNode.get("highestBid").asDouble();

            pairQuotes.put(pair, new PairQuote(pair, base, quote, new BigDecimal(bid), new BigDecimal(ask)));
        }

        return pairQuotes;
    }
}

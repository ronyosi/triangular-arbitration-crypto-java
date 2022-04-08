package com.webhopper.uniswap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.utils.JsonFacade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class UniswapService {
    final UniswapApi uniswapApi;

    public UniswapService(UniswapApi uniswapApi) {
        this.uniswapApi = uniswapApi;
    }

    public static void main(String[] args) {
        UniswapService uniswapService = new UniswapService(new UniswapApi());
        Map<String, PairQuote> pricingInfo = uniswapService.getPricingInfo();
        System.out.println();
    }

    public Map<String, PairQuote> getPricingInfo() {
        final String json = uniswapApi.getPricesFromFileOrApiCall(false);
        return mapUniswapJsonToPairQuotes(json);
    }

    private static Map<String, PairQuote> mapUniswapJsonToPairQuotes(String prices) {

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

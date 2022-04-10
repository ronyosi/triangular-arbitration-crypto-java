package com.webhopper.integrations.uniswap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.integrations.poloniex.Quote;
import com.webhopper.integrations.poloniex.UniswapQuote;
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
        Map<String, Quote> pricingInfo = uniswapService.getPricingInfo();
        System.out.println();
    }

    public Map<String, Quote> getPricingInfo() {
        final String json = uniswapApi.getPricesFromFileOrApiCall(false);
        return mapUniswapJsonToPairQuotes(json);
    }

    private Map<String, Quote> mapUniswapJsonToPairQuotes(String prices) {
        ObjectMapper objectMapper = JsonFacade.getObjectMapper();
        JsonNode priceData = null;
        try {
            priceData = objectMapper.readValue(prices, JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Map<String, Quote> pairQuotes = new HashMap<>();
        final JsonNode liquidityPools = priceData.get("data").get("pools");
        Iterator<JsonNode> iterator = liquidityPools.iterator();
        while (iterator.hasNext()) {
            final JsonNode poolEntry = iterator.next();

            final JsonNode contractId = poolEntry.get("id");
            String base = poolEntry.get("token0").get("symbol").asText();
            String quote = poolEntry.get("token1").get("symbol").asText();
            final String pair = base + "_" + quote;

            double token0Price = poolEntry.get("token0Price").asDouble();
            double token1Price = poolEntry.get("token1Price").asDouble();

            pairQuotes.put(pair, new UniswapQuote(pair, base, quote, new BigDecimal(token1Price), new BigDecimal(token0Price)));
        }

        return pairQuotes;
    }
}

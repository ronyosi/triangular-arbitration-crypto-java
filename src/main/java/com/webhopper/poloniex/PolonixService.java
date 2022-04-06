package com.webhopper.poloniex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webhopper.utils.FileUtils;
import com.webhopper.utils.JsonFacade;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class PolonixService {
    final PoloniexApi poloniexApi;

    public PolonixService(PoloniexApi poloniexApi) {
        this.poloniexApi = poloniexApi;
    }

    public Map<String, PairQuote> getPricingInfo() {
        final String json = poloniexApi.getPricesFromFileOrApiCall(false);
        return mapPoloniexJsonToPairQuotes(json);
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

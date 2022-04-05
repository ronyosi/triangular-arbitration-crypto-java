package com.webhopper.poloniex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webhopper.FileUtils;
import com.webhopper.JsonFacade;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class PolonixApiFacade {

    public static Map<String, PairQuote> getPrices(boolean disableCache) {
        String tickers = null;

        if(!disableCache) {
            try {
                tickers = FileUtils.openFile("tickers.json");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (tickers != null) {
                return mapPoloniexJsonToPairQuotes(tickers);
            }
        }

        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder(
                URI.create("https://poloniex.com/public?command=returnTicker"))
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

        FileUtils.writeFile(response.body(), "tickers.json");

        return mapPoloniexJsonToPairQuotes(response.body());
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

            pairQuotes.put(pair, new PairQuote(pair, base, quote, bid, ask));
        }

        return pairQuotes;
    }
}

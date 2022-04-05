package com.webhopper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.webhopper.poloniex.PairQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

public class StructureTriangles {
    private static final Logger logger = LoggerFactory.getLogger(StructureTriangles.class);

    private String getPrices() {
        String tickers = null;

        try {
            tickers = FileUtils.openFile("tickers.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(tickers != null) {
            return tickers;
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

        return response.body();
    }



    public List<Triangle> structure()  {
        final List<Triangle> result = new LinkedList<>();
        final Set<String> trianglesAlreadyFound = new HashSet<>();
        final String prices = getPrices();
        ObjectNode tradableCoins = filterTradableCoins(prices);
        List<PairQuote> pairQuotes = mapPoloniexJsonToPairQuotes(tradableCoins);
        
        for(PairQuote pairA : pairQuotes) {
            final String baseA = pairA.getBase();
            final String quoteA = pairA.getQuote();
            for(PairQuote pairB : pairQuotes) {
                if (pairA.equals(pairB)) {
                    // skip bcs pairA should != pairB
                    continue;
                }
                final String baseB = pairB.getBase();
                final String quoteB = pairB.getQuote();

                if (aPairNotTradableForBPair(baseA, quoteA, baseB, quoteB)) {
                    continue;
                }
                final Set<String> coinsToCompleteTriangle = findCoinsNeededToCompleteTriangle(baseA, quoteA, baseB, quoteB);
                for(PairQuote pairC : pairQuotes) {
                    if (pairC.equals(pairA) || pairC.equals(pairB)) {
                        continue;
                    }

                    final String baseC = pairC.getBase();
                    final String quoteC = pairC.getQuote();

                    // pairC must contain both coins to complete triangle.
                    if (!coinsToCompleteTriangle.contains(baseC) || !coinsToCompleteTriangle.contains(quoteC)) {
                        continue;
                    }

                    List<String> combineAll = Arrays.asList(new String[]{pairA.getPair(), pairB.getPair(), pairC.getPair()});
                    Collections.sort(combineAll);
                    final String uniqueItem = String.join(", ", combineAll);
                    if (!trianglesAlreadyFound.contains(uniqueItem)) {
                        logger.debug("Found triangular pair: {} => {} => {}", pairA, pairB, pairC);
                        trianglesAlreadyFound.add(uniqueItem);

                        Triangle triangle = new Triangle();
                        triangle.setBaseA(baseA);
                        triangle.setBaseB(baseB);
                        triangle.setBaseC(baseC);
                        triangle.setQuoteA(quoteA);
                        triangle.setQuoteB(quoteB);
                        triangle.setQuoteC(quoteC);
                        triangle.setPairA(pairA.getPair());
                        triangle.setPairA(pairB.getPair());
                        triangle.setPairA(pairC.getPair());
                        triangle.setCombined(pairA + "," + pairB + "," + pairC);
                        result.add(triangle);
                    }
                }
            }
        }

        return result;
    }

    private List<PairQuote> mapPoloniexJsonToPairQuotes(ObjectNode priceData) {
        List<PairQuote> pairQuotes = new ArrayList<>();

        final Iterator<String> pairNames = priceData.fieldNames();

        while (pairNames.hasNext()) {
            final String pair = pairNames.next();
//            JsonNode jsonNode = priceData.get(pair);
            String[] split = pair.split("_");
            String base = split[0];
            String quote = split[1];

            pairQuotes.add(new PairQuote(pair, base, quote));
        }

        return pairQuotes;
    }

    private Set<String> findCoinsNeededToCompleteTriangle(String aBase, String aQuote, String bBase, String bQuote) {
        Set<String> result = new HashSet<>();
        if(!aBase.equals(bBase) && !aBase.equals(bQuote)) {
            result.add(aBase);
        }
        if(!aQuote.equals(bBase) && !aQuote.equals(bQuote)) {
            result.add(aQuote);
        }

        if(!bBase.equals(aBase) && !bBase.equals(aQuote)) {
            result.add(bBase);
        }

        if(!bQuote.equals(aBase) && !bQuote.equals(aQuote)) {
            result.add(bQuote);
        }

        return result;
    }

    private boolean aPairNotTradableForBPair(String aBase, String aQuote, String bBase, String bQuote) {
        return !aBase.equals(bBase) && !aBase.equals(bQuote) && !aQuote.equals(bBase) && !aQuote.equals(bQuote);
    }

    private ObjectNode filterTradableCoins(String prices) {
        ObjectMapper objectMapper = JsonFacade.getObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readValue(prices, JsonNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        ObjectNode result = objectMapper.createObjectNode();

        long initialCoinCount = jsonNode.size();
        logger.info("initial coin count" + initialCoinCount);

        Iterator<String> iterator = jsonNode.fieldNames();
        while(iterator.hasNext()) {
            String pairName = iterator.next();
            final JsonNode pairInfo = jsonNode.get(pairName);
            if(pairInfo.get("isFrozen").asInt() != 1 && pairInfo.get("postOnly").asInt() != 1) {
                result.set(pairName, pairInfo);
            }
        }

//        for(String coin : jsonNode.fieldNames()) {
//            JsonObject obj = jsonObject.get(coin).getAsJsonObject();
//            int isFrozen = obj.get("isFrozen").getAsInt();
//            int postOnly = obj.get("postOnly").getAsInt();
//            if(isFrozen != 1 && postOnly != 1) {
//                result.add(coin, obj);
//            }
//
//        }
        long afterFilterCoinCount = result.size();

        System.out.println("after filter coin count" + afterFilterCoinCount);
        return result;
    }

}

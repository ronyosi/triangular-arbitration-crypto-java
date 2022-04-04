package com.webhopper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.attribute.HashAttributeSet;
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

        JsonObject tradableCoins = filterTradableCoins(prices);
        for(String pairA : tradableCoins.keySet()) {
            JsonObject pairAInfo = tradableCoins.get(pairA).getAsJsonObject();
            String[] split = pairA.split("_");
            String aBase = split[0];
            String aQuote = split[1];

            for(String pairB : tradableCoins.keySet()) {
                if(pairA.equals(pairB)) {
                    // skip bcs pairA should != pairB
                    continue;
                }

                String[] splitPairB = pairB.split("_");
                String bBase = splitPairB[0];
                String bQuote = splitPairB[1];

                if(aPairNotTradableForBPair(aBase, aQuote, bBase, bQuote)) {
                    continue;
                }

                final Set<String> coinsToCompleteTriangle = findCoinsNeededToCompleteTriangle(aBase, aQuote, bBase, bQuote);
                for(String pairC : tradableCoins.keySet()) {
                    if(pairC.equals(pairA) || pairC.equals(pairB)){
                        continue;
                    }

                    final String[] splitPairC = pairC.split("_");
                    String cBase = splitPairC[0];
                    String cQuote = splitPairC[1];

                    // pairC must contain both coins to complete triangle.
                    if(!coinsToCompleteTriangle.contains(cBase) || !coinsToCompleteTriangle.contains(cQuote)) {
                        continue;
                    }

                    List<String> combineAll = Arrays.asList(new String[]{pairA, pairB, pairC});
                    Collections.sort(combineAll);
                    final String uniqueItem = String.join(", ", combineAll);
                    if(!trianglesAlreadyFound.contains(uniqueItem)) {
                        logger.debug("Found triangular pair: {} => {} => {}", pairA, pairB, pairC);
                        trianglesAlreadyFound.add(uniqueItem);

                        Triangle triangle = new Triangle();
                        triangle.setBaseA(aBase);
                        triangle.setBaseB(bBase);
                        triangle.setBaseC(cBase);
                        triangle.setQuoteA(aQuote);
                        triangle.setQuoteB(bQuote);
                        triangle.setQuoteC(cQuote);
                        triangle.setPairA(pairA);
                        triangle.setPairA(pairB);
                        triangle.setPairA(pairC);
                        triangle.setCombined(pairA + "," + pairB + "," + pairC);
                        result.add(triangle);
                    }





                }
            }
         }


        return result;
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

    private JsonObject filterTradableCoins(String prices) {
        JsonObject jsonObject = new JsonParser().parse(prices).getAsJsonObject();

        JsonObject result = new JsonObject();

        long initialCoinCount = jsonObject.keySet().stream().count();
        logger.info("initial coin count" + initialCoinCount);

        Map<String, String> filteredCoins = new HashMap<>();

        for(String coin : jsonObject.keySet()) {
            JsonObject obj = jsonObject.get(coin).getAsJsonObject();
            int isFrozen = obj.get("isFrozen").getAsInt();
            int postOnly = obj.get("postOnly").getAsInt();
            if(isFrozen != 1 && postOnly != 1) {
                result.add(coin, obj);
            }

        }
        long afterFilterCoinCount = result.keySet().stream().count();

        System.out.println("after filter coin count" + afterFilterCoinCount);
        return result;
    }

}

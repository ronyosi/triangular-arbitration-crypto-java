package com.webhopper.poloniex;

import com.webhopper.utils.FileUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class PoloniexApi {
    public String getPricesFromFileOrApiCall(boolean disableCache) {
        String tickers = null;

        if(!disableCache) {
            try {
                tickers = FileUtils.openFile("tickers.json");
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (tickers != null) {
                return tickers;
            }
        }

        final String jsonString = httpGetPrices();
        FileUtils.writeFile(jsonString, "tickers.json");
        return jsonString;
    }


    protected static String httpGetPrices() {
        String tickers = null;

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
        return response.body();
    }
}

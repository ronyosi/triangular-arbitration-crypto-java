package com.webhopper;

import java.util.Objects;

public class Triangle {
    Pair a;
    Pair b;
    Pair c;
    private String baseA;
    private String baseB;
    private String baseC;
    private String quoteA;
    private String quoteB;
    private String quoteC;
    private String pairA;
    private String pairB;
    private String pairC;
    private String combined;

    public String getBaseA() {
        return baseA;
    }

    public void setBaseA(String baseA) {
        this.baseA = baseA;
    }

    public String getBaseB() {
        return baseB;
    }

    public void setBaseB(String baseB) {
        this.baseB = baseB;
    }

    public String getBaseC() {
        return baseC;
    }

    public void setBaseC(String baseC) {
        this.baseC = baseC;
    }

    public String getQuoteA() {
        return quoteA;
    }

    public void setQuoteA(String quoteA) {
        this.quoteA = quoteA;
    }

    public String getQuoteB() {
        return quoteB;
    }

    public void setQuoteB(String quoteB) {
        this.quoteB = quoteB;
    }

    public String getQuoteC() {
        return quoteC;
    }

    public void setQuoteC(String quoteC) {
        this.quoteC = quoteC;
    }

    public String getPairA() {
        return pairA;
    }

    public void setPairA(String pairA) {
        this.pairA = pairA;
    }

    public String getPairB() {
        return pairB;
    }

    public void setPairB(String pairB) {
        this.pairB = pairB;
    }

    public String getPairC() {
        return pairC;
    }

    public void setPairC(String pairC) {
        this.pairC = pairC;
    }

    public String getCombined() {
        return combined;
    }

    public void setCombined(String combined) {
        this.combined = combined;
    }

    @Override
    public String toString() {
        return "Triangle{" +
                "combined='" + combined + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Triangle triangle = (Triangle) o;
        return Objects.equals(baseA, triangle.baseA) &&
                Objects.equals(baseB, triangle.baseB) &&
                Objects.equals(baseC, triangle.baseC) &&
                Objects.equals(quoteA, triangle.quoteA) &&
                Objects.equals(quoteB, triangle.quoteB) &&
                Objects.equals(quoteC, triangle.quoteC) &&
                Objects.equals(pairA, triangle.pairA) &&
                Objects.equals(pairB, triangle.pairB) &&
                Objects.equals(pairC, triangle.pairC) &&
                Objects.equals(combined, triangle.combined);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseA, baseB, baseC, quoteA, quoteB, quoteC, pairA, pairB, pairC, combined);
    }

    static class Pair {
        String base;
        String quote;
        String pair;

        public Pair(String base, String quote, String pair) {
            this.base = base;
            this.quote = quote;
            this.pair = pair;
        }
    }
}

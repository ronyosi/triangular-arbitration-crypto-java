package com.webhopper.entities;

import java.util.Objects;

public class Triangle {
    private Pair pairA;
    private Pair pairB;
    private Pair pairC;
//    private String baseA;
//    private String baseB;
//    private String baseC;
//    private String quoteA;
//    private String quoteB;
//    private String quoteC;
//    private String pairA;
//    private String pairB;
//    private String pairC;
    private String combined;

    public Pair getPairA() {
        return pairA;
    }

    public void setPairA(Pair a) {
        this.pairA = a;
    }

    public Pair getPairB() {
        return pairB;
    }

    public void setPairB(Pair b) {
        this.pairB = b;
    }

    public Pair getPairC() {
        return pairC;
    }

    public void setPairC(Pair c) {
        this.pairC = c;
    }

//    public String getBaseA() {
//        return baseA;
//    }
//
//    public void setBaseA(String baseA) {
//        this.baseA = baseA;
//    }
//
//    public String getBaseB() {
//        return baseB;
//    }
//
//    public void setBaseB(String baseB) {
//        this.baseB = baseB;
//    }
//
//    public String getBaseC() {
//        return baseC;
//    }
//
//    public void setBaseC(String baseC) {
//        this.baseC = baseC;
//    }
//
//    public String getQuoteA() {
//        return quoteA;
//    }
//
//    public void setQuoteA(String quoteA) {
//        this.quoteA = quoteA;
//    }
//
//    public String getQuoteB() {
//        return quoteB;
//    }
//
//    public void setQuoteB(String quoteB) {
//        this.quoteB = quoteB;
//    }
//
//    public String getQuoteC() {
//        return quoteC;
//    }
//
//    public void setQuoteC(String quoteC) {
//        this.quoteC = quoteC;
//    }

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
        return Objects.equals(pairA, triangle.pairA) &&
                Objects.equals(pairB, triangle.pairB) &&
                Objects.equals(pairC, triangle.pairC) &&
                Objects.equals(combined, triangle.combined);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pairA, pairB, pairC, combined);
    }
}

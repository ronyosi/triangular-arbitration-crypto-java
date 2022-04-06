package com.webhopper.business;

import com.webhopper.entities.Pair;
import com.webhopper.entities.Triangle;
import com.webhopper.poloniex.PairQuote;
import com.webhopper.poloniex.PolonixService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class StructureTriangles {
    private static final Logger logger = LoggerFactory.getLogger(StructureTriangles.class);

    final PolonixService polonixService;

    public StructureTriangles(final PolonixService polonixService) {
        this.polonixService = polonixService;
    }



    public List<Triangle> structure()  {
        final List<Triangle> result = new LinkedList<>();
        final Set<String> trianglesAlreadyFound = new HashSet<>();
        final Map<String, PairQuote> pairQuotes = polonixService.getPricingInfo();

        for(PairQuote pairA : pairQuotes.values()) {
            final String baseA = pairA.getBase();
            final String quoteA = pairA.getQuote();
            for(PairQuote pairB : pairQuotes.values()) {
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
                for(PairQuote pairC : pairQuotes.values()) {
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
                        triangle.setA(new Pair(baseA, quoteA, pairA.getPair()));
                        triangle.setB(new Pair(baseB, quoteB, pairB.getPair()));
                        triangle.setC(new Pair(baseC, quoteC, pairC.getPair()));
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
}

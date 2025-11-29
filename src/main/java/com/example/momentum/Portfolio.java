
package com.example.momentum;

import java.util.*;

public class Portfolio {

    private final Map<String, Double> weightsBySymbol = new HashMap<>();

    public Map<String, Double> getWeightsBySymbol() {
        return Collections.unmodifiableMap(weightsBySymbol);
    }

    public void setEqualWeights(List<String> symbols) {
        weightsBySymbol.clear();
        if (symbols == null || symbols.isEmpty()) {
            weightsBySymbol.put("CASH", 1.0);
            return;
        }
        double w = 1.0 / symbols.size();
        for (String s : symbols) {
            weightsBySymbol.put(s, w);
        }
    }
}

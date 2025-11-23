
package com.example.momentum;

import java.time.LocalDate;
import java.util.*;

public class RelativeStrengthBacktester {

    private final Map<String, EtfHistory> universe;
    private final RelativeStrengthRanker ranker;
    private final int topN;

    private final String benchmarkSymbol; // e.g. SPY
    private final String safetySymbol;    // e.g. IEF or CASH proxy
    private final int maPeriod;           // e.g. 200-day MA for risk-on/off

    public RelativeStrengthBacktester(Map<String, EtfHistory> universe,
                                      RelativeStrengthRanker ranker,
                                      int topN,
                                      String benchmarkSymbol,
                                      String safetySymbol,
                                      int maPeriod) {
        this.universe = universe;
        this.ranker = ranker;
        this.topN = topN;
        this.benchmarkSymbol = benchmarkSymbol;
        this.safetySymbol = safetySymbol;
        this.maPeriod = maPeriod;

        if (!universe.containsKey(benchmarkSymbol)) {
            throw new IllegalArgumentException("Universe does not contain benchmark: " + benchmarkSymbol);
        }
        if (!universe.containsKey(safetySymbol)) {
            throw new IllegalArgumentException("Universe does not contain safety asset: " + safetySymbol);
        }
    }

    public EquityCurve runBacktest(double initialCapital) {
        if (universe.isEmpty()) {
            throw new IllegalStateException("Universe is empty");
        }

        EtfHistory reference = universe.get(benchmarkSymbol);
        List<LocalDate> rebalanceDates = DateUtils.monthEndTradingDays(reference);

        Portfolio portfolio = new Portfolio();
        double equity = initialCapital;

        List<LocalDate> equityDates = new ArrayList<>();
        List<Double> equityValues = new ArrayList<>();

        for (int i = 0; i < rebalanceDates.size() - 1; i++) {
            LocalDate rebalanceDate = rebalanceDates.get(i);
            LocalDate nextRebalanceDate = rebalanceDates.get(i + 1);

            boolean riskOn = isRiskOn(rebalanceDate);

            List<String> selected = new ArrayList<>();

            if (!riskOn) {
                selected.add(safetySymbol);
            } else {
                List<RankedEtf> ranked = ranker.rank(universe, rebalanceDate);

                for (RankedEtf r : ranked) {
                    if (r.symbol().equals(safetySymbol)) {
                        continue; // don't treat safety asset as a risk asset
                    }
                    if (r.relativeStrength() <= 0.0) {
                        break;
                    }
                    selected.add(r.symbol());
                    if (selected.size() == topN) break;
                }

                if (selected.isEmpty()) {
                    selected.add(safetySymbol);
                }
            }

            portfolio.setEqualWeights(selected);

            double periodReturn = computePortfolioReturn(portfolio, rebalanceDate, nextRebalanceDate);
            equity *= (1.0 + periodReturn);

            equityDates.add(nextRebalanceDate);
            equityValues.add(equity);

            System.out.printf("%s -> %s | riskOn=%s | holdings=%s | periodRet=%.2f%% | equity=%.2f%n",
                    rebalanceDate, nextRebalanceDate, riskOn, selected, periodReturn * 100.0, equity);
        }

        return new EquityCurve(equityDates, equityValues);
    }

    private boolean isRiskOn(LocalDate asOfDate) {
        EtfHistory benchmark = universe.get(benchmarkSymbol);

        OptionalDouble maOpt = TechnicalIndicators.simpleMovingAverage(benchmark, asOfDate, maPeriod);
        OptionalDouble priceOpt = benchmark.getCloseOnOrBefore(asOfDate);

        if (maOpt.isEmpty() || priceOpt.isEmpty()) {
            return false;
        }

        double ma = maOpt.getAsDouble();
        double price = priceOpt.getAsDouble();

        return price >= ma;
    }

    private double computePortfolioReturn(Portfolio portfolio, LocalDate from, LocalDate to) {
        Map<String, Double> weights = portfolio.getWeightsBySymbol();

        if (weights.isEmpty() || (weights.size() == 1 && weights.containsKey("CASH"))) {
            return 0.0;
        }

        double total = 0.0;
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            String symbol = e.getKey();
            double weight = e.getValue();

            if ("CASH".equals(symbol)) {
                continue;
            }

            EtfHistory history = universe.get(symbol);
            if (history == null) continue;

            OptionalDouble pFromOpt = history.getCloseOnOrBefore(from);
            OptionalDouble pToOpt = history.getCloseOnOrBefore(to);

            if (pFromOpt.isEmpty() || pToOpt.isEmpty()) continue;

            double ret = (pToOpt.getAsDouble() / pFromOpt.getAsDouble()) - 1.0;
            total += weight * ret;
        }

        return total;
    }
}

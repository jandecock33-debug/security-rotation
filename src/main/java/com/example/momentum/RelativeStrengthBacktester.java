package com.example.momentum;

import java.time.LocalDate;
import java.util.*;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class RelativeStrengthBacktester {

    private final Map<String, EtfHistory> universe;
    private final EtfRanker ranker;
    private final int topN;

    private final String benchmarkSymbol; // e.g. SPY
    private final String safetySymbol;    // e.g. IEF
    private final int maPeriod;           // e.g. 200-day MA
    private final RotationSpeed rotationSpeed;
    private final int slowKeepRankMultiplier;
    private final int atrPeriodDays = 20; // for ATR%
    private final Path rankingsCsvPath;

    public RelativeStrengthBacktester(Map<String, EtfHistory> universe,
                                      EtfRanker ranker,
                                      int topN,
                                      String benchmarkSymbol,
                                      String safetySymbol,
                                      int maPeriod,
                                      RotationSpeed rotationSpeed,
                                      int slowKeepRankMultiplier) {
        this.universe = universe;
        this.ranker = ranker;
        this.topN = topN;
        this.benchmarkSymbol = benchmarkSymbol;
        this.safetySymbol = safetySymbol;
        this.maPeriod = maPeriod;
        this.rotationSpeed = rotationSpeed;
        this.slowKeepRankMultiplier = slowKeepRankMultiplier;

        if (!universe.containsKey(benchmarkSymbol)) {
            throw new IllegalArgumentException("Universe does not contain benchmark: " + benchmarkSymbol);
        }
        if (!universe.containsKey(safetySymbol)) {
            throw new IllegalArgumentException("Universe does not contain safety asset: " + safetySymbol);
        }

        this.rankingsCsvPath = Paths.get("output", "ranked-universe.csv");
        initCsv();
    }

    private void initCsv() {
        try {
            Files.createDirectories(rankingsCsvPath.getParent());
            String header = "date,scoreMode,rotationSpeed,riskOn,rank,symbol,score,dailyScore,weeklyScore,monthlyScore,atrPercent,isHolding%n";
            Files.writeString(
                    rankingsCsvPath,
                    String.format(header),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            System.err.println("Could not initialize rankings CSV: " + e.getMessage());
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

        List<String> previousHoldings = new ArrayList<>();

        for (int i = 0; i < rebalanceDates.size() - 1; i++) {
            LocalDate rebalanceDate = rebalanceDates.get(i);
            LocalDate nextRebalanceDate = rebalanceDates.get(i + 1);

            // Always compute ranking (for score display), even if we go risk-off.
            List<RankedEtf> ranked = ranker.rank(universe, rebalanceDate);
            Map<String, RankedEtf> rankedBySymbol = ranked.stream()
                    .collect(Collectors.toMap(RankedEtf::symbol, r -> r));

            boolean riskOn = isRiskOn(rebalanceDate);

            List<String> selected;

            if (!riskOn) {
                selected = List.of(safetySymbol);
            } else {
                selected = selectHoldings(ranked, previousHoldings);
            }

            portfolio.setEqualWeights(selected);
            previousHoldings = selected;

            double periodReturn = computePortfolioReturn(portfolio, rebalanceDate, nextRebalanceDate);
            equity *= (1.0 + periodReturn);

            equityDates.add(nextRebalanceDate);
            equityValues.add(equity);

            String holdingsDetails = selected.stream()
                    .map(sym -> {
                        RankedEtf r = rankedBySymbol.get(sym);
                        OptionalDouble atrPctOpt = TechnicalIndicators.atrPercent(
                                universe.get(sym), rebalanceDate, atrPeriodDays);

                        String scoreStr = formatScoreForHolding(r);
                        String atrStr = atrPctOpt.isEmpty()
                                ? "n/a"
                                : String.format("%.2f", atrPctOpt.getAsDouble());
                        return sym + "(" + scoreStr + ",ATR%=" + atrStr + ")";
                    })
                    .collect(Collectors.joining(", "));

            System.out.printf(
                    "%s -> %s | riskOn=%s | rotation=%s | scoreMode=%s | holdings=[%s] | periodRet=%.2f%% | equity=%.2f%n",
                    rebalanceDate, nextRebalanceDate, riskOn, rotationSpeed, ranker.getMode(),
                    holdingsDetails, periodReturn * 100.0, equity);

            // full ranked list in console
            printRankedUniverse(rebalanceDate, ranked);

            // write ranked universe to CSV
            appendRankingsCsv(rebalanceDate, riskOn, selected, ranked);
        }

        return new EquityCurve(equityDates, equityValues);
    }

    private String formatScoreForHolding(RankedEtf r) {
        if (r == null) return "score=n/a";

        return switch (ranker.getMode()) {
            case RS_COMBINED -> String.format("RS=%.2f", r.score());
            case RETURN_6M -> String.format("R6M%%=%.2f", r.score());
            case TV_TECHNICAL -> String.format("TV(M/W/D)=%.2f/%.2f/%.2f", r.monthly(), r.weekly(), r.daily());
        };
    }

    private String formatScoreForRow(RankedEtf r) {
        if (r == null) return "n/a";
        return switch (ranker.getMode()) {
            case RS_COMBINED -> String.format(Locale.US, "RS=%.2f", r.score());
            case RETURN_6M -> String.format(Locale.US, "R6M%%=%.2f", r.score());
            case TV_TECHNICAL -> String.format(Locale.US, "M=%.2f W=%.2f D=%.2f", r.monthly(), r.weekly(), r.daily());
        };
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

    private List<String> selectHoldings(List<RankedEtf> ranked, List<String> previousHoldings) {
        if (rotationSpeed == RotationSpeed.FAST) {
            return selectFast(ranked);
        } else {
            return selectSlow(ranked, previousHoldings);
        }
    }

    private List<String> selectFast(List<RankedEtf> ranked) {
        List<String> selected = new ArrayList<>();
        for (RankedEtf r : ranked) {
            if (r.symbol().equals(safetySymbol)) continue;
            if (r.score() <= 0.0) break;
            selected.add(r.symbol());
            if (selected.size() == topN) break;
        }
        if (selected.isEmpty()) {
            selected = List.of(safetySymbol);
        }
        return selected;
    }

    /**
     * Slow rotation:
     * - Try to keep existing holdings as long as they stay reasonably strong
     *   (still in top N * slowKeepRankMultiplier and score > 0).
     * - Only replace them when they drop further down the ranking, or when
     *   there are clearly stronger candidates available.
     */
    private List<String> selectSlow(List<RankedEtf> ranked, List<String> previousHoldings) {
        int keepRankLimit = topN * slowKeepRankMultiplier;

        Map<String, Integer> rankIndex = new HashMap<>();
        Map<String, RankedEtf> bySymbol = new HashMap<>();
        for (int i = 0; i < ranked.size(); i++) {
            RankedEtf r = ranked.get(i);
            rankIndex.put(r.symbol(), i + 1); // 1-based rank
            bySymbol.put(r.symbol(), r);
        }

        List<String> selected = new ArrayList<>();

        // 1) Try to keep previous risky holdings that are still strong enough
        List<String> previousRisky = new ArrayList<>();
        for (String sym : previousHoldings) {
            if (sym.equals("CASH") || sym.equals(safetySymbol)) continue;
            previousRisky.add(sym);
        }

        // Filter: must still be in ranking, score > 0, and rank <= keepRankLimit
        List<String> keepCandidates = new ArrayList<>();
        for (String sym : previousRisky) {
            RankedEtf r = bySymbol.get(sym);
            Integer rank = rankIndex.get(sym);
            if (r == null || rank == null) continue;
            if (r.score() <= 0.0) continue;
            if (rank > keepRankLimit) continue;
            keepCandidates.add(sym);
        }

        // Sort kept ones by current rank (best first)
        keepCandidates.sort(Comparator.comparingInt(rankIndex::get));

        for (String sym : keepCandidates) {
            if (selected.size() >= topN) break;
            selected.add(sym);
        }

        // 2) Fill remaining slots with strongest non-selected ETFs
        for (RankedEtf r : ranked) {
            if (selected.size() >= topN) break;
            if (r.symbol().equals(safetySymbol)) continue;
            if (r.score() <= 0.0) break;
            if (selected.contains(r.symbol())) continue;
            selected.add(r.symbol());
        }

        if (selected.isEmpty()) {
            selected = List.of(safetySymbol);
        }

        return selected;
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

    // print full ranked list to console
    private void printRankedUniverse(LocalDate asOfDate, List<RankedEtf> ranked) {

        System.out.println("  Ranked universe on " + asOfDate + " (" + ranker.getMode() + "):");
        int pos = 1;
        for (RankedEtf r : ranked) {
            EtfHistory h = universe.get(r.symbol());
            OptionalDouble atrPctOpt = TechnicalIndicators.atrPercent(h, asOfDate, atrPeriodDays);
            String atrStr = atrPctOpt.isEmpty()
                    ? "n/a"
                    : String.format("%.2f", atrPctOpt.getAsDouble());

            System.out.printf("    #%d %s: %s, ATR%%=%s%n",
                    pos++, r.symbol(), formatScoreForRow(r), atrStr);
        }
    }

    // write ranked universe to CSV
    private void appendRankingsCsv(LocalDate date,
                                   boolean riskOn,
                                   List<String> selected,
                                   List<RankedEtf> ranked) {
        try {
            StringBuilder sb = new StringBuilder();
            int pos = 1;
            for (RankedEtf r : ranked) {
                EtfHistory h = universe.get(r.symbol());
                OptionalDouble atrPctOpt = TechnicalIndicators.atrPercent(h, date, atrPeriodDays);

                String scoreStr = String.format(Locale.US, "%.6f", r.score());
                String dailyStr = Double.isNaN(r.daily()) ? "" : String.format(Locale.US, "%.6f", r.daily());
                String weeklyStr = Double.isNaN(r.weekly()) ? "" : String.format(Locale.US, "%.6f", r.weekly());
                String monthlyStr = Double.isNaN(r.monthly()) ? "" : String.format(Locale.US, "%.6f", r.monthly());

                // For single-score modes, keep dailyScore populated for convenience
                if (ranker.getMode() != ScoreMode.TV_TECHNICAL) {
                    weeklyStr = "";
                    monthlyStr = "";
                }

                String atrStr = atrPctOpt.isEmpty()
                        ? ""
                        : String.format(Locale.US, "%.4f", atrPctOpt.getAsDouble());
                boolean holding = selected.contains(r.symbol());

                sb.append(date).append(',')
                        .append(ranker.getMode()).append(',')
                        .append(rotationSpeed).append(',')
                        .append(riskOn).append(',')
                        .append(pos++).append(',')
                        .append(r.symbol()).append(',')
                        .append(scoreStr).append(',')
                        .append(dailyStr).append(',')
                        .append(weeklyStr).append(',')
                        .append(monthlyStr).append(',')
                        .append(atrStr).append(',')
                        .append(holding)
                        .append('\n');
            }

            Files.writeString(rankingsCsvPath, sb.toString(), StandardOpenOption.APPEND);
        } catch (Exception e) {
            System.err.println("Could not append rankings CSV: " + e.getMessage());
        }
    }
}

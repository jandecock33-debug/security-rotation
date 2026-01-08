
package com.example.momentum;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import com.example.momentum.db.ChartingDbClient;
import com.example.momentum.db.DbConfig;
import com.example.momentum.db.UniverseFileParser;
import com.example.momentum.db.UniverseResolver;

import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Working dir: " + System.getProperty("user.dir"));

        Path universeFile = args.length >= 1 ? Path.of(args[0]) : Path.of("config/universe.txt");
        Path dbPropertiesFile = args.length >= 2 ? Path.of(args[1]) : DbConfig.DEFAULT_PROPERTIES_PATH;

        RotationSpeed rotationSpeed = askRotationSpeedFromUser();
        ScoreMode scoreMode = askScoreModeFromUser();

        // If you want automatic downloads from Stooq, uncomment this:
        // downloadFromStooq();

        Map<String, EtfHistory> universe = loadUniverseFromDatabase(universeFile, dbPropertiesFile);

        // Signal calculators
        ScoreCalculator calculator;
        if (scoreMode == ScoreMode.RS_COMBINED) {
            int lookback3m = 63;
            int lookback6m = 126;
            int lookback12m = 252;
            calculator = new CombinedMomentumCalculator(lookback3m, lookback6m, lookback12m);
        } else if (scoreMode == ScoreMode.RETURN_6M) {
            calculator = new SixMonthReturnCalculator(120); // ~6 months
        } else {
            calculator = new TvTechnicalScoreCalculator();
        }

        EtfRanker ranker = new EtfRanker(calculator, scoreMode);

        int topN = 3;
        int maPeriod = 200; // 200-day MA for risk-on/off
        String benchmark = "SPY";
        String safety = "IEF";

        int slowKeepRankMultiplier = 2; // keep holdings while they remain in top (N * 2)
        RelativeStrengthBacktester backtester = new RelativeStrengthBacktester(
                universe, ranker, topN, benchmark, safety, maPeriod, rotationSpeed, slowKeepRankMultiplier
        );

        double initialCapital = 100_000.0;
        EquityCurve curve = backtester.runBacktest(initialCapital);

        double finalEquity = curve.equity().isEmpty()
                ? initialCapital
                : curve.equity().get(curve.equity().size() - 1);

        System.out.println("Initial capital: " + initialCapital);
        System.out.println("Final equity: " + finalEquity);
        System.out.println("Ranked universe CSV written to output/ranked-universe.csv");

        plotEquityCurve(curve, rotationSpeed, scoreMode);
    }

    private static RotationSpeed askRotationSpeedFromUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose rotation speed:");
        System.out.println("  1 = FAST (aggressive: always jump to current top-N)");
        System.out.println("  2 = SLOW (stickier: keep holdings as long as they remain strong)");
        System.out.print("Your choice [1/2]: ");
        String input = scanner.nextLine().trim();
        if ("2".equals(input)) {
            return RotationSpeed.SLOW;
        }
        return RotationSpeed.FAST;
    }

    private static ScoreMode askScoreModeFromUser() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose ranking mode:");
        System.out.println("  1 = Combined RS (3/6/12-month momentum)");
        System.out.println("  2 = Last 6 months return (%)");
        System.out.println("  3 = TradingView Technical Score (Daily/Weekly/Monthly)");
        System.out.print("Your choice [1/2/3]: ");
        String input = scanner.nextLine().trim();
        if ("2".equals(input)) {
            return ScoreMode.RETURN_6M;
        }
        if ("3".equals(input)) {
            return ScoreMode.TV_TECHNICAL;
        }
        return ScoreMode.RS_COMBINED;
    }

    private static Map<String, EtfHistory> loadUniverseFromDatabase(Path universeFile, Path dbPropertiesFile) throws Exception {
        // Optional: trim history if you only want data from a given date
        LocalDate startDate = LocalDate.of(2002, 1, 1);

        DbConfig dbConfig = DbConfig.loadOrThrow(dbPropertiesFile);
        Set<String> tokens = UniverseFileParser.parse(universeFile);

        Map<String, EtfHistory> universe = new HashMap<>();
        try (ChartingDbClient db = new ChartingDbClient(dbConfig)) {
            Set<String> symbols = UniverseResolver.resolve(tokens, db);

            // Always include benchmark & safety, because the backtester depends on them.
            symbols.add("SPY");
            symbols.add("IEF");

            System.out.println("Universe tokens: " + tokens);
            System.out.println("Resolved symbols: " + symbols.size());

            for (String s : symbols) {
                try {
                    EtfHistory history = db.loadHistory(s, startDate);
                    if (history.getBars().isEmpty()) {
                        System.out.println("WARN: No price history found in DB for symbol " + s + " (skipping)");
                        continue;
                    }
                    universe.put(s, history);
                } catch (Exception e) {
                    System.out.println("WARN: Failed to load history for " + s + ": " + e.getMessage());
                }
            }
        }

        if (universe.isEmpty()) {
            throw new IllegalStateException("Universe is empty. Check your universe file and DB connection.");
        }
        return universe;
    }

    private static EtfHistory trimHistoryFrom(EtfHistory original, LocalDate startDate) {
        List<PriceBar> filtered = original.getBars().stream()
                .filter(b -> !b.date().isBefore(startDate))
                .collect(Collectors.toList());

        return new EtfHistory(original.getSymbol(), filtered);
    }

    private static void plotEquityCurve(EquityCurve curve, RotationSpeed speed, ScoreMode mode) {
        List<Date> xData = curve.dates().stream()
                .map(Date::valueOf)
                .collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
                .width(900)
                .height(600)
                .title("Dual-Momentum ETF Strategy (" + speed + ", " + mode + ") - Equity Curve")
                .xAxisTitle("Date")
                .yAxisTitle("Equity")
                .build();

        chart.addSeries("Strategy", xData, curve.equity());

        new SwingWrapper<>(chart).displayChart();
    }
}


package com.example.momentum;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws Exception {
        System.out.println("Working dir: " + System.getProperty("user.dir"));

        RotationSpeed rotationSpeed = askRotationSpeedFromUser();

        // If you want automatic downloads from Stooq, uncomment this:
        // downloadFromStooq();

        Map<String, EtfHistory> universe = loadUniverseFromCsv();

        // Combined momentum: 3/6/12 months (~63/126/252 trading days)
        int lookback3m = 63;
        int lookback6m = 126;
        int lookback12m = 252;

        int topN = 3;
        int maPeriod = 200; // 200-day MA for risk-on/off
        String benchmark = "SPY";
        String safety = "IEF";

        RelativeStrengthCalculator calc = new RelativeStrengthCalculator(lookback3m, lookback6m, lookback12m);
        RelativeStrengthRanker ranker = new RelativeStrengthRanker(calc);

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

        plotEquityCurve(curve, rotationSpeed);
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

    private static void downloadFromStooq() throws IOException, InterruptedException {
        StooqDownloader dl = new StooqDownloader();
        dl.downloadDailyCsv("spy.us", Path.of("data/SPY_stooq.csv"));
        dl.downloadDailyCsv("efa.us", Path.of("data/EFA_stooq.csv"));
        dl.downloadDailyCsv("qqq.us", Path.of("data/QQQ_stooq.csv"));
        dl.downloadDailyCsv("iwm.us", Path.of("data/IWM_stooq.csv"));
        dl.downloadDailyCsv("ief.us", Path.of("data/IEF_stooq.csv"));
    }

    private static Map<String, EtfHistory> loadUniverseFromCsv() throws IOException {
        Map<String, EtfHistory> universe = new HashMap<>();

        // Expect CSV files downloaded from Stooq (or manually saved) in the data/ folder:
        //   https://stooq.com/q/d/l/?s=spy.us&i=d  -> SPY_stooq.csv
        universe.put("SPY", StooqCsvLoader.load("SPY", Path.of("data/SPY_stooq.csv")));
        universe.put("EFA", StooqCsvLoader.load("EFA", Path.of("data/EFA_stooq.csv")));
        universe.put("QQQ", StooqCsvLoader.load("QQQ", Path.of("data/QQQ_stooq.csv")));
        universe.put("IWM", StooqCsvLoader.load("IWM", Path.of("data/IWM_stooq.csv")));
        universe.put("IEF", StooqCsvLoader.load("IEF", Path.of("data/IEF_stooq.csv"))); // safety asset (bonds)
        universe.put("GLD", StooqCsvLoader.load("GLD", Path.of("data/GLD_stooq.csv")));
        universe.put("NVDA", StooqCsvLoader.load("NVDA", Path.of("data/NVDA_stooq.csv")));
        universe.put("SLV", StooqCsvLoader.load("SLV", Path.of("data/SLV_stooq.csv")));
        universe.put("SOXL", StooqCsvLoader.load("SOXL", Path.of("data/SOXL_stooq.csv")));
        universe.put("TQQQ", StooqCsvLoader.load("TQQQ", Path.of("data/TQQQ_stooq.csv")));
        universe.put("GOOG", StooqCsvLoader.load("GOOG", Path.of("data/GOOG_stooq.csv")));
        universe.put("UBER", StooqCsvLoader.load("UBER", Path.of("data/UBER_stooq.csv")));
        universe.put("COIN", StooqCsvLoader.load("COIN", Path.of("data/COIN_stooq.csv")));  // kathy Wood 251124
//        universe.put("BMNR", StooqCsvLoader.load("BMNR", Path.of("data/BMNR_stooq.csv")));   // kathy Wood 251124
        universe.put("CRCL", StooqCsvLoader.load("CRCL", Path.of("data/CRCL_stooq.csv")));  // kathy Wood 251124
        universe.put("CDE", StooqCsvLoader.load("CDE", Path.of("data/CDE_stooq.csv")));
        universe.put("CPER", StooqCsvLoader.load("CPER", Path.of("data/CPER_stooq.csv")));
        universe.put("UUP", StooqCsvLoader.load("UUP", Path.of("data/UUP_stooq.csv")));
        universe.put("TLT", StooqCsvLoader.load("TLT", Path.of("data/TLT_stooq.csv")));
        universe.put("BABA", StooqCsvLoader.load("BABA", Path.of("data/BABA_stooq.csv")));  // kathy Wood 251124
        universe.put("AMC", StooqCsvLoader.load("AMC", Path.of("data/AMC_stooq.csv")));
//        universe.put("UAMY", StooqCsvLoader.load("UAMY", Path.of("data/UAMY_stooq.csv")));
        universe.put("APP", StooqCsvLoader.load("APP", Path.of("data/APP_stooq.csv")));
        universe.put("OPEN", StooqCsvLoader.load("OPEN", Path.of("data/OPEN_stooq.csv")));
        universe.put("OPFI", StooqCsvLoader.load("OPFI", Path.of("data/OPFI_stooq.csv")));
        universe.put("URA", StooqCsvLoader.load("URA", Path.of("data/URA_stooq.csv")));

        // Optional: trim history if you only want data from a given date
        LocalDate startDate = LocalDate.of(2002, 1, 1);
        for (Map.Entry<String, EtfHistory> e : new ArrayList<>(universe.entrySet())) {
            universe.put(e.getKey(), trimHistoryFrom(e.getValue(), startDate));
        }

        return universe;
    }

    private static EtfHistory trimHistoryFrom(EtfHistory original, LocalDate startDate) {
        List<PriceBar> filtered = original.getBars().stream()
                .filter(b -> !b.date().isBefore(startDate))
                .collect(Collectors.toList());

        return new EtfHistory(original.getSymbol(), filtered);
    }

    private static void plotEquityCurve(EquityCurve curve, RotationSpeed speed) {
        List<Date> xData = curve.dates().stream()
                .map(Date::valueOf)
                .collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
                .width(900)
                .height(600)
                .title("Dual-Momentum ETF Strategy (" + speed + " rotation) - Equity Curve")
                .xAxisTitle("Date")
                .yAxisTitle("Equity")
                .build();

        chart.addSeries("Strategy", xData, curve.equity());

        new SwingWrapper<>(chart).displayChart();
    }
}


package com.example.momentum;

import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

  // see https://chatgpt.com/g/g-p-68e394f7d2588191be07ef22e8e6952b/c/691cde60-a480-8326-8cee-463b0856bc0d
    public static void main(String[] args) throws IOException {
        Map<String, EtfHistory> universe = new HashMap<>();

        // Download CSVs from Yahoo Finance and save them into the data/ folder with these filenames:
        // SPY.csv, EFA.csv, IWM.csv, QQQ.csv, IEF.csv
//        universe.put("SPY", CsvLoader.loadYahooCsv("SPY", Path.of("data/SPY.csv")));
//        universe.put("EFA", CsvLoader.loadYahooCsv("EFA", Path.of("data/EFA.csv")));
//        universe.put("IWM", CsvLoader.loadYahooCsv("IWM", Path.of("data/IWM.csv")));
//        universe.put("QQQ", CsvLoader.loadYahooCsv("QQQ", Path.of("data/QQQ.csv")));
//        universe.put("IEF", CsvLoader.loadYahooCsv("IEF", Path.of("data/IEF.csv"))); // safety asset (bonds)

      // ORI :
//      universe.put("SPY", StooqCsvLoader.load("SPY", Path.of("data/SPY_stooq.csv")));
//      universe.put("EFA", StooqCsvLoader.load("EFA", Path.of("data/EFA_stooq.csv")));
//      universe.put("QQQ", StooqCsvLoader.load("QQQ", Path.of("data/QQQ_stooq.csv")));
//      universe.put("IWM", StooqCsvLoader.load("IWM", Path.of("data/IWM_stooq.csv")));
//      universe.put("IEF", StooqCsvLoader.load("IEF", Path.of("data/IEF_stooq.csv")));

      universe.put("SPY", StooqCsvLoader.load("SPY", Path.of("data/SPY_stooq.csv")));
      universe.put("EFA", StooqCsvLoader.load("EFA", Path.of("data/EFA_stooq.csv")));
      universe.put("QQQ", StooqCsvLoader.load("QQQ", Path.of("data/QQQ_stooq.csv")));
      universe.put("IWM", StooqCsvLoader.load("IWM", Path.of("data/IWM_stooq.csv")));
      universe.put("IEF", StooqCsvLoader.load("IEF", Path.of("data/IEF_stooq.csv")));
      universe.put("NVDA", StooqCsvLoader.load("NVDA", Path.of("data/NVDA_stooq.csv")));
      universe.put("GLD", StooqCsvLoader.load("GLD", Path.of("data/GLD_stooq.csv")));
      universe.put("SLV", StooqCsvLoader.load("SLV", Path.of("data/SLV_stooq.csv")));
      universe.put("TQQQ", StooqCsvLoader.load("TQQQ", Path.of("data/TQQQ_stooq.csv")));
      universe.put("SOXL", StooqCsvLoader.load("SOXL", Path.of("data/SOXL_stooq.csv")));
      universe.put("SQQQ", StooqCsvLoader.load("SQQQ", Path.of("data/SQQQ_stooq.csv")));

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
        RelativeStrengthBacktester backtester = new RelativeStrengthBacktester(universe, ranker, topN, benchmark, safety, maPeriod);

        double initialCapital = 100_000.0;
        EquityCurve curve = backtester.runBacktest(initialCapital);

        double finalEquity = curve.equity().isEmpty()
                ? initialCapital
                : curve.equity().get(curve.equity().size() - 1);

        System.out.println("Initial capital: " + initialCapital);
        System.out.println("Final equity: " + finalEquity);

        // Plot equity curve using XChart
        plotEquityCurve(curve);
    }

    private static void plotEquityCurve(EquityCurve curve) {
        List<Date> xData = curve.dates().stream()
                .map(Date::valueOf)
                .collect(Collectors.toList());

        XYChart chart = new XYChartBuilder()
                .width(900)
                .height(600)
                .title("Dual-Momentum ETF Strategy - Equity Curve")
                .xAxisTitle("Date")
                .yAxisTitle("Equity")
                .build();

        chart.addSeries("Strategy", xData, curve.equity());

        new SwingWrapper<>(chart).displayChart();
    }
}

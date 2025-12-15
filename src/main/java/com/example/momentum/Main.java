
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
        ScoreMode scoreMode = askScoreModeFromUser();

        // If you want automatic downloads from Stooq, uncomment this:
        // downloadFromStooq();

        Map<String, EtfHistory> universe = loadUniverseFromCsv();

        // Signal calculators
        ScoreCalculator calculator;
        if (scoreMode == ScoreMode.RS_COMBINED) {
            int lookback3m = 63;
            int lookback6m = 126;
            int lookback12m = 252;
            calculator = new CombinedMomentumCalculator(lookback3m, lookback6m, lookback12m);
        } else {
            calculator = new SixMonthReturnCalculator(120); // ~6 months
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
        System.out.print("Your choice [1/2]: ");
        String input = scanner.nextLine().trim();
        if ("2".equals(input)) {
            return ScoreMode.RETURN_6M;
        }
        return ScoreMode.RS_COMBINED;
    }

    private static void downloadFromStooq() throws IOException, InterruptedException {
    }

    private static Map<String, EtfHistory> loadUniverseFromCsv() throws IOException {
        Map<String, EtfHistory> universe = new HashMap<>();

        // Expect CSV files downloaded from Stooq (or manually saved) in the data/ folder:
        //   https://stooq.com/q/d/l/?s=spy.us&i=d  -> SPY_stooq.csv
      /*
       * Individuals
       */
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
        universe.put("BMNR", StooqCsvLoader.load("BMNR", Path.of("data/BMNR_stooq.csv")));   // kathy Wood 251124
        universe.put("CRCL", StooqCsvLoader.load("CRCL", Path.of("data/CRCL_stooq.csv")));  // kathy Wood 251124
        universe.put("CDE", StooqCsvLoader.load("CDE", Path.of("data/CDE_stooq.csv")));
        universe.put("CPER", StooqCsvLoader.load("CPER", Path.of("data/CPER_stooq.csv")));
        universe.put("UUP", StooqCsvLoader.load("UUP", Path.of("data/UUP_stooq.csv")));
        universe.put("TLT", StooqCsvLoader.load("TLT", Path.of("data/TLT_stooq.csv")));
        universe.put("BABA", StooqCsvLoader.load("BABA", Path.of("data/BABA_stooq.csv")));  // kathy Wood 251124
        universe.put("AMC", StooqCsvLoader.load("AMC", Path.of("data/AMC_stooq.csv")));
//        universe.put("UAMY", StooqCsvLoader.load("UAMY", Path.of("data/UAMY_stooq.csv")));
        universe.put("APP", StooqCsvLoader.load("APP", Path.of("data/APP_stooq.csv")));
//        universe.put("OPEN", StooqCsvLoader.load("OPEN", Path.of("data/OPEN_stooq.csv")));
        universe.put("OPFI", StooqCsvLoader.load("OPFI", Path.of("data/OPFI_stooq.csv")));
        universe.put("URA", StooqCsvLoader.load("URA", Path.of("data/URA_stooq.csv")));
        universe.put("NU", StooqCsvLoader.load("NU", Path.of("data/NU_stooq.csv")));
        universe.put("SMH", StooqCsvLoader.load("SMH", Path.of("data/SMH_stooq.csv")));
        universe.put("EMQQ", StooqCsvLoader.load("EMQQ", Path.of("data/EMQQ_stooq.csv")));
        universe.put("REMX", StooqCsvLoader.load("REMX", Path.of("data/REMX_stooq.csv")));
        universe.put("RGLD", StooqCsvLoader.load("RGLD", Path.of("data/RGLD_stooq.csv")));
        universe.put("PLTR", StooqCsvLoader.load("PLTR", Path.of("data/PLTR_stooq.csv")));
        universe.put("AAPL", StooqCsvLoader.load("AAPL", Path.of("data/AAPL_stooq.csv")));
        universe.put("MSFT", StooqCsvLoader.load("MSFT", Path.of("data/MSFT_stooq.csv")));
        universe.put("TSLA", StooqCsvLoader.load("TSLA", Path.of("data/TSLA_stooq.csv")));
        universe.put("GDX", StooqCsvLoader.load("GDX", Path.of("data/GDX_stooq.csv")));
        universe.put("DBA", StooqCsvLoader.load("DBA", Path.of("data/DBA_stooq.csv")));
        universe.put("CORN", StooqCsvLoader.load("CORN", Path.of("data/CORN_stooq.csv")));
        universe.put("SOYB", StooqCsvLoader.load("SOYB", Path.of("data/SOYB_stooq.csv")));
        universe.put("WEAT", StooqCsvLoader.load("WEAT", Path.of("data/WEAT_stooq.csv")));
        universe.put("DBB", StooqCsvLoader.load("DBB", Path.of("data/DBB_stooq.csv")));
        universe.put("BCIM", StooqCsvLoader.load("BCIM", Path.of("data/BCIM_stooq.csv")));
        universe.put("USO", StooqCsvLoader.load("USO", Path.of("data/USO_stooq.csv")));
        universe.put("BNO", StooqCsvLoader.load("BNO", Path.of("data/BNO_stooq.csv")));
        universe.put("IAU", StooqCsvLoader.load("IAU", Path.of("data/IAU_stooq.csv")));
        universe.put("FGDL", StooqCsvLoader.load("FGDL", Path.of("data/FGDL_stooq.csv")));
        universe.put("PLG", StooqCsvLoader.load("PLG", Path.of("data/PLG_stooq.csv")));
        universe.put("FIGS", StooqCsvLoader.load("FIGS", Path.of("data/FIGS_stooq.csv")));
        universe.put("MDB", StooqCsvLoader.load("MDB", Path.of("data/MDB_stooq.csv")));
        universe.put("KMT", StooqCsvLoader.load("KMT", Path.of("data/KMT_stooq.csv")));
        universe.put("EVER", StooqCsvLoader.load("EVER", Path.of("data/EVER_stooq.csv")));
        universe.put("TRGP", StooqCsvLoader.load("TRGP", Path.of("data/TRGP_stooq.csv")));
        universe.put("AEM", StooqCsvLoader.load("AEM", Path.of("data/AEM_stooq.csv")));
//
//        // commodities
//        universe.put("GDX", StooqCsvLoader.load("GDX", Path.of("data/GDX_stooq.csv")));


      /*
       * Core benchmark + safety - see https://chatgpt.com/g/g-p-68e394f7d2588191be07ef22e8e6952b-software-projecten/c/691cde60-a480-8326-8cee-463b0856bc0d
       */
//        universe.put("SPY", StooqCsvLoader.load("SPY", Path.of("data/SPY_stooq.csv")));
//        universe.put("EFA", StooqCsvLoader.load("EFA", Path.of("data/EFA_stooq.csv")));
//        universe.put("IWM", StooqCsvLoader.load("IWM", Path.of("data/IWM_stooq.csv")));
//        universe.put("IEF", StooqCsvLoader.load("IEF", Path.of("data/IEF_stooq.csv")));


        /*
         * Sector ETFs (SPDR Select Sectors)
         * These are all US-domiciled, very liquid, and match S&P 500 sectors. Perfect for rotation:
         */
//        universe.put("XLK", StooqCsvLoader.load("XLK", Path.of("data/XLK_stooq.csv")));
//        universe.put("XLF", StooqCsvLoader.load("XLF", Path.of("data/XLF_stooq.csv")));
//        universe.put("XLV", StooqCsvLoader.load("XLV", Path.of("data/XLV_stooq.csv")));
//        universe.put("XLE", StooqCsvLoader.load("XLE", Path.of("data/XLE_stooq.csv")));
//        universe.put("XLI", StooqCsvLoader.load("XLI", Path.of("data/XLI_stooq.csv")));
//        universe.put("XLP", StooqCsvLoader.load("XLP", Path.of("data/XLP_stooq.csv")));
//        universe.put("XLY", StooqCsvLoader.load("XLY", Path.of("data/XLY_stooq.csv")));
//        universe.put("XLB", StooqCsvLoader.load("XLB", Path.of("data/XLB_stooq.csv")));
//        universe.put("XLU", StooqCsvLoader.load("XLU", Path.of("data/XLU_stooq.csv")));
//        universe.put("XLRE", StooqCsvLoader.load("XLRE", Path.of("data/XLRE_stooq.csv")));
//        universe.put("XLC", StooqCsvLoader.load("XLC", Path.of("data/XLC_stooq.csv")));




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

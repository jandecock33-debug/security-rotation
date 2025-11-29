
# Java ETF Dual-Momentum Backtester (with rotation speed control)

Features:
- CSV loader for Stooq daily data (Date,Open,High,Low,Close,Volume).
- Combined momentum score using 3/6/12-month lookbacks (63/126/252 trading days).
- Risk-off rule: switch to a safety asset (e.g., bond ETF `IEF`) when the benchmark (`SPY`) is below its 200-day SMA.
- Monthly rotation into the top-N ETFs by combined momentum.
- **Rotation speed modes**:
  - FAST: aggressive rotation (always jump to current top-N every month).
  - SLOW: stickier rotation (keep existing holdings as long as they remain in roughly the top 2N and RS > 0).
- Equity curve plotting using XChart.

## How to use

1. Download historical daily data from Stooq:

   - SPY: https://stooq.com/q/d/l/?s=spy.us&i=d
   - EFA: https://stooq.com/q/d/l/?s=efa.us&i=d
   - QQQ: https://stooq.com/q/d/l/?s=qqq.us&i=d
   - IWM: https://stooq.com/q/d/l/?s=iwm.us&i=d
   - IEF: https://stooq.com/q/d/l/?s=ief.us&i=d

   Save them into the `data/` folder as:

   - `SPY_stooq.csv`
   - `EFA_stooq.csv`
   - `QQQ_stooq.csv`
   - `IWM_stooq.csv`
   - `IEF_stooq.csv`

   Alternatively, uncomment `downloadFromStooq()` in `Main` to let the app download them.

2. Open this project in IntelliJ as a Maven project (open `pom.xml`).

3. Run the `Main` class.
   - It will ask you to choose rotation speed:
     - `1` = FAST
     - `2` = SLOW
   - It then runs the backtest with that mode and opens the equity curve chart.

4. You can adjust:
   - The ETF universe and safety asset in `loadUniverseFromCsv()`.
   - Lookback windows in `Main` and behavior of `RelativeStrengthCalculator`.
   - The slow rotation aggressiveness via `slowKeepRankMultiplier` in `Main` (default is 2).

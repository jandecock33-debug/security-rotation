
# Java ETF Dual-Momentum Backtester

Features:
- CSV loader using `Files.lines(...)` and `String.split(",")` for Yahoo Finance data.
- Combined momentum score using 3/6/12-month lookbacks (63/126/252 trading days).
- Risk-off rule: switch to a safety asset (e.g., bond ETF `IEF`) when the benchmark (`SPY`) is below its 200-day SMA.
- Monthly rotation into the top-N ETFs by combined momentum, subject to the risk-on/off filter.
- Equity curve plotting using XChart.

## How to use

1. Download historical daily data for:
   - SPY
   - EFA
   - IWM
   - QQQ
   - IEF (or another bond ETF)

   from Yahoo Finance as CSV (Historical Data -> Download).

2. Place the CSV files into the `data/` folder with these filenames:
   - `SPY.csv`
   - `EFA.csv`
   - `IWM.csv`
   - `QQQ.csv`
   - `IEF.csv`

3. Open this project in IntelliJ as a Maven project.

4. Run the `Main` class.
   - You should see log lines per rebalance period in the console.
   - A chart window will pop up showing the equity curve.

You can change:
- The ETF universe and safety asset in `Main`.
- Lookback windows and the 200-day MA period.
- `RelativeStrengthCalculator` if you want different weighting of the lookbacks.
# security-rotation

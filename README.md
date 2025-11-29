
# Java ETF Dual-Momentum Backtester (rotation + signal mode + ATR%)

Features:
- CSV loader for Stooq daily data (Date,Open,High,Low,Close,Volume).
- Monthly dual-momentum strategy with:
  - Risk-on/off using 200-day SMA of SPY.
  - Top-N ETF selection among risky assets.
- Rotation speed modes:
  - FAST: aggressively jump to current top-N every month.
  - SLOW: keep existing holdings as long as they remain in roughly top 2N and score > 0.
- Signal modes (ranking metric):
  - RS_COMBINED: 3/6/12-month combined momentum (63/126/252 days).
  - RETURN_6M: simple last 6-month return in percent (~120 trading days).
- ATR% calculation (20-day ATR / close * 100):
  - For every selected ETF on each rebalance, console output includes:
    - its score (RS or 6M%) and
    - its ATR% at the rebalance date.
- Equity curve plotting using XChart.

## Data

Download historical daily data from Stooq:

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

Or uncomment `downloadFromStooq()` in `Main` to let the app download them.

## Running

1. Open the project in IntelliJ as a Maven project (open `pom.xml`).

2. Run the `Main` class.
   - First, it will ask rotation speed:
     - `1` = FAST
     - `2` = SLOW
   - Then, it will ask ranking mode:
     - `1` = 3/6/12-month combined RS
     - `2` = last 6-month return (%)

3. For each monthly period, the console prints:
   - riskOn/riskOff,
   - rotation mode,
   - score mode,
   - holdings **including score and ATR%**,
   - period return and equity.

   Example snippet:

   2015-01-30 -> 2015-02-27 | riskOn=true | rotation=FAST | scoreMode=RS_COMBINED |
     holdings=[QQQ(RS=0.18,ATR%=1.45),IWM(RS=0.15,ATR%=1.80),SPY(RS=0.12,ATR%=1.10)] |
     periodRet=2.34% | equity=102345.67

4. An XChart window pops up with the equity curve for that configuration.

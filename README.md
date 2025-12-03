
# Java ETF Dual-Momentum Backtester (Complete Version)

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
- ATR% calculation (20-day ATR / close * 100).
- Console output per rebalance:
  - Original summary line with holdings, score (RS or 6M%) and ATR%.
  - Full ranked universe list, sorted by current score mode, with ATR%.
- CSV export of the ranked universe:
  - Written to `output/ranked-universe.csv`.
  - Columns: date, scoreMode, rotationSpeed, riskOn, rank, symbol, score, atrPercent, isHolding.

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
   - period return and equity,
   - full ranked universe list.

4. The file `output/ranked-universe.csv` contains the full ranking history for use in Excel/R, etc.

5. An XChart window pops up with the equity curve for that configuration.

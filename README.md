
# Java ETF Dual-Momentum Backtester (Complete Version)

This version reads both the **universe** and the **daily price history** from your PostgreSQL
database (the "charting" DB).

The symbols to process are defined in an input file, which can contain:

- Individual tickers (e.g. `AAPL`)
- `SP500` (expand to all symbols where `quotes.origin` contains `SP500`)
- `NASDAQ100` (expand to all symbols where `quotes.origin` contains `NASDAQ100`)

You can comment out lines to exclude them.

## Features

- (Still included) CSV loader for Stooq daily data (Date,Open,High,Low,Close,Volume).
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

## Input files

### 1) Database connection

Create `config/charting-db.properties` (sample provided):

```properties
url=jdbc:postgresql://localhost:5432/charting
username=postgres
password=postgres
```

Or provide the same values via environment variables / system properties:

- `CHARTING_DB_URL`
- `CHARTING_DB_USERNAME`
- `CHARTING_DB_PASSWORD`

### 2) Universe definition

Edit `config/universe.txt` (sample provided). Example:

```text
SP500
NASDAQ100

# Individual tickers:
AAPL
MSFT  # inline comment

// TSLA  (commented out)
;NVDA  (commented out)
```

## Running

1. Open the project in IntelliJ as a Maven project (open `pom.xml`).

2. Run the `Main` class.

   By default it uses:
   - universe file: `config/universe.txt`
   - DB config file: `config/charting-db.properties`

   You can override them with arguments:
   - `args[0]` = universe file path
   - `args[1]` = db properties path
   - First, it will ask rotation speed:
     - `1` = FAST
     - `2` = SLOW
   - Then, it will ask ranking mode:
     - `1` = 3/6/12-month combined RS
     - `2` = last 6-month return (%)
     - `3` = TradingView Technical Score (Daily/Weekly/Monthly)

   Note: the app always tries to load `SPY` (benchmark) and `IEF` (safety) from the DB,
   even if they are not listed in `universe.txt`.

3. For each monthly period, the console prints:
   - riskOn/riskOff,
   - rotation mode,
   - score mode,
   - holdings **including score and ATR%**,
   - period return and equity,
   - full ranked universe list.

4. The file `output/ranked-universe.csv` contains the full ranking history for use in Excel/R, etc.

5. An XChart window pops up with the equity curve for that configuration.

where do I find a .csv file of the stock or ETF prices?

You’ve got a few easy options; the most common free one is **Yahoo Finance**, plus some alternatives.

---

## 1. Yahoo Finance (most common, free)

**Steps:**

1. Go to Yahoo Finance and search your symbol (e.g. `SPY` or `AAPL`).
2. Open the **“Historical Data”** tab.
3. Click **“Time Period”** → set your desired date range.
4. Set **“Show”** to “Historical Prices”.
5. Click **“Apply”**.
6. Hit **“Download”** → you get a `.csv` with Date, Open, High, Low, Close, Adj Close, Volume.

You can then load that CSV into Java (e.g. with `Files.lines(...)`) and map rows to your `PriceBar` objects.

---

## 2. Stooq (simple, good for ETFs too)

Stooq offers easy CSV downloads:

1. Search for your symbol on Stooq (e.g. `SPY.US`).
2. There’s an option to download historical data as CSV (different timeframes: daily, weekly, etc.).

Nice if you want another source or symbols Yahoo doesn’t handle well.

---

## 3. API-style options (more work, but automatable)

If you want your Java code to **download and update prices automatically**, look at:

* **Alpha Vantage** (free tier with API key, returns JSON/CSV).
* **Tiingo** (paid but cheap, nice clean API).
* Your **broker’s API** (IBKR, etc.) if you’re already using one.

Your Java app would:

* Call the API (via `HttpClient`),
* Parse JSON/CSV,
* Store to local CSV or directly in memory.

---

## 4. How this fits your Java backtester

Once you’ve got a CSV (from Yahoo or anywhere):

* Columns you care about: `Date` and `Adj Close` (or `Close`).
* Parse each line into `PriceBar(LocalDate date, double close)` and dump them into an `EtfHistory`.

If you want, I can:

* Show you a **concrete Java CSV loader** that reads a Yahoo Finance file (e.g. `SPY.csv`) into your `EtfHistory` map.


source : https://chatgpt.com/c/691cde60-a480-8326-8cee-463b0856bc0d


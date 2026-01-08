Here you go — full project with all the extras you asked for (multi-lookback momentum, risk-off with 200-day MA, CSV loader, and XChart equity curve plot):

What’s inside

Maven project (pom.xml) with:

org.knowm.xchart:xchart:3.8.8 dependency

Core classes (under src/main/java/com/example/momentum):

PriceBar – simple OHLC wrapper (we use date + close)

EtfHistory – holds price history and lookup helpers

CsvLoader – loads Yahoo Finance CSVs using Files.lines() + String.split(",")

RelativeStrengthCalculator – combined momentum using 3/6/12-month lookbacks (63/126/252 days)

RelativeStrengthRanker – ranks ETFs by that combined momentum

TechnicalIndicators – includes simpleMovingAverage for the 200-day MA

Portfolio – equal-weight allocation

DateUtils – month-end trading days

RelativeStrengthBacktester – dual-momentum logic:

relative strength ranking

risk-off rule: if benchmark (e.g. SPY) is below its 200-day MA → allocate to safety asset (e.g. IEF)

EquityCurve – holds dates + equity values

Main – wires everything together, runs the backtest, plots equity curve with XChart

data/ folder

Empty, ready for you to drop in:

SPY.csv, EFA.csv, IWM.csv, QQQ.csv, IEF.csv

README.md

Short instructions to run & tweak.

How to run in IntelliJ

Download the zip above and extract it.

In IntelliJ: File → Open… → select pom.xml (open as Maven project).

Download daily CSVs from Yahoo Finance for SPY/EFA/IWM/QQQ/IEF and put them in the data/ folder with those exact filenames.

Run the Main class.

Console will show rebalance logs.

A window pops up with the equity curve chart.

If you want, next step I can help you tweak:

the universe (e.g. sectors, factors, countries),

the weighting of 3/6/12m lookbacks,

or add a benchmark / buy & hold comparison line to the chart.

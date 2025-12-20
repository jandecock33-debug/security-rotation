package com.example.momentum;

import java.util.ArrayList;
import java.util.List;

/**
 * Produces a TradingView-like technical rating based on a set of 15 moving-average / filter rules
 * and 11 oscillator rules.
 *
 * Each constituent produces -1 (Sell), 0 (Neutral), +1 (Buy). Group scores are the simple mean.
 */
public final class TvRater {

    public record TvTimeframeScore(double maScore, double oscScore, double overallScore) {}

    public static TvTimeframeScore rate(List<PriceBar> barsAscending) {
        if (barsAscending == null || barsAscending.isEmpty()) {
            return new TvTimeframeScore(Double.NaN, Double.NaN, Double.NaN);
        }

        int n = barsAscending.size();
        int end = n - 1;

        double[] open = new double[n];
        double[] high = new double[n];
        double[] low = new double[n];
        double[] close = new double[n];
        double[] vol = new double[n];

        for (int i = 0; i < n; i++) {
            PriceBar b = barsAscending.get(i);
            open[i] = b.open();
            high[i] = b.high();
            low[i] = b.low();
            close[i] = b.close();
            vol[i] = b.volume();
        }

        double lastClose = close[end];

        // --- Moving Averages (15) ---
        List<Integer> maRatings = new ArrayList<>(15);
        maRatings.add(comparePriceToValue(lastClose, sma(close, end, 10)));
        maRatings.add(comparePriceToValue(lastClose, ema(close, end, 10)));
        maRatings.add(comparePriceToValue(lastClose, sma(close, end, 20)));
        maRatings.add(comparePriceToValue(lastClose, ema(close, end, 20)));
        maRatings.add(comparePriceToValue(lastClose, sma(close, end, 30)));
        maRatings.add(comparePriceToValue(lastClose, ema(close, end, 30)));
        maRatings.add(comparePriceToValue(lastClose, sma(close, end, 50)));
        maRatings.add(comparePriceToValue(lastClose, ema(close, end, 50)));
        maRatings.add(comparePriceToValue(lastClose, sma(close, end, 100)));
        maRatings.add(comparePriceToValue(lastClose, ema(close, end, 100)));
        maRatings.add(comparePriceToValue(lastClose, sma(close, end, 200)));
        maRatings.add(comparePriceToValue(lastClose, ema(close, end, 200)));
        maRatings.add(comparePriceToValue(lastClose, vwma(close, vol, end, 20)));
        maRatings.add(comparePriceToValue(lastClose, hma(close, end, 9)));
        maRatings.add(comparePriceToValue(lastClose, ichimokuBaseLine(high, low, end, 26)));

        double maScore = avgSigned(maRatings);

        // --- Oscillators (11) ---
        List<Integer> oscRatings = new ArrayList<>(11);

        // RSI(14): Buy if <30, Sell if >70
        oscRatings.add(threshold(rsi(close, end, 14), 30, 70));

        // Stochastic(14,3,3)
        StochKD stoch = stochastic(high, low, close, end, 14, 3);
        oscRatings.add(stochRule(stoch.k(), stoch.d()));

        // CCI(20)
        oscRatings.add(cciRule(cci(high, low, close, end, 20)));

        // ADX(14) with +DI / -DI
        AdxPack adx = adx(high, low, close, end, 14);
        oscRatings.add(adxRule(adx.adx(), adx.plusDi(), adx.minusDi()));

        // Awesome Oscillator: SMA(median,5) - SMA(median,34)
        oscRatings.add(sign(awesomeOscillator(high, low, end)));

        // Momentum(10): close - close[10]
        oscRatings.add(sign(momentum(close, end, 10)));

        // MACD(12,26,9): Buy if MACD > signal
        MacdPack macd = macd(close, end, 12, 26, 9);
        oscRatings.add(crossRule(macd.macd(), macd.signal()));

        // Stoch RSI(14,14,3,3)
        StochKD stochRsi = stochasticRsi(close, end, 14, 14, 3);
        oscRatings.add(stochRule(stochRsi.k(), stochRsi.d()));

        // Williams %R(14): Buy if < -80, Sell if > -20
        oscRatings.add(williamsRule(williamsR(high, low, close, end, 14)));

        // Bull/Bear Power (13)
        BullBear bb = bullBearPower(high, low, close, end, 13);
        oscRatings.add(bbPowerRule(bb.bull(), bb.bear()));

        // Ultimate Oscillator(7,14,28)
        oscRatings.add(ultimateRule(ultimateOscillator(high, low, close, end, 7, 14, 28)));

        double oscScore = avgSigned(oscRatings);
        double overall = (maScore + oscScore) / 2.0;

        return new TvTimeframeScore(maScore, oscScore, overall);
    }

    // ---------- Rating helpers ----------

    private static int comparePriceToValue(double price, double value) {
        if (Double.isNaN(value)) return 0;
        if (price > value) return +1;
        if (price < value) return -1;
        return 0;
    }

    private static double avgSigned(List<Integer> ratings) {
        if (ratings.isEmpty()) return Double.NaN;
        int sum = 0;
        for (int r : ratings) sum += r;
        return sum / (double) ratings.size();
    }

    private static int sign(double v) {
        if (Double.isNaN(v)) return 0;
        return v > 0 ? +1 : (v < 0 ? -1 : 0);
    }

    private static int threshold(double v, double buyBelow, double sellAbove) {
        if (Double.isNaN(v)) return 0;
        if (v < buyBelow) return +1;
        if (v > sellAbove) return -1;
        return 0;
    }

    private static int crossRule(double v, double signal) {
        if (Double.isNaN(v) || Double.isNaN(signal)) return 0;
        if (v > signal) return +1;
        if (v < signal) return -1;
        return 0;
    }

    private static int stochRule(double k, double d) {
        if (Double.isNaN(k) || Double.isNaN(d)) return 0;
        if (k < 20 && d < 20 && k > d) return +1;
        if (k > 80 && d > 80 && k < d) return -1;
        return 0;
    }

    private static int cciRule(double cci) {
        if (Double.isNaN(cci)) return 0;
        if (cci < -100) return +1;
        if (cci > 100) return -1;
        return 0;
    }

    private static int adxRule(double adx, double plusDi, double minusDi) {
        if (Double.isNaN(adx) || Double.isNaN(plusDi) || Double.isNaN(minusDi)) return 0;
        if (adx <= 20) return 0;
        if (plusDi > minusDi) return +1;
        if (plusDi < minusDi) return -1;
        return 0;
    }

    private static int williamsRule(double wr) {
        if (Double.isNaN(wr)) return 0;
        if (wr < -80) return +1;
        if (wr > -20) return -1;
        return 0;
    }

    private static int ultimateRule(double uo) {
        if (Double.isNaN(uo)) return 0;
        if (uo > 70) return +1;
        if (uo < 30) return -1;
        return 0;
    }

    private static int bbPowerRule(double bull, double bear) {
        if (Double.isNaN(bull) || Double.isNaN(bear)) return 0;
        if (bull > 0 && bear > 0) return +1;
        if (bull < 0 && bear < 0) return -1;
        return 0;
    }

    // ---------- Indicator calculations (last value only) ----------

    private static double sma(double[] v, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double sum = 0.0;
        for (int i = start; i <= end; i++) sum += v[i];
        return sum / period;
    }

    private static double ema(double[] v, int end, int period) {
        if (end < period - 1) return Double.NaN;
        double alpha = 2.0 / (period + 1.0);
        // seed with SMA of first period
        double ema = 0.0;
        for (int i = 0; i < period; i++) ema += v[i];
        ema /= period;
        for (int i = period; i <= end; i++) {
            ema = alpha * v[i] + (1.0 - alpha) * ema;
        }
        return ema;
    }

    private static double vwma(double[] close, double[] vol, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double num = 0.0;
        double den = 0.0;
        for (int i = start; i <= end; i++) {
            num += close[i] * vol[i];
            den += vol[i];
        }
        if (den == 0.0) return Double.NaN;
        return num / den;
    }

    private static double wma(double[] v, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double num = 0.0;
        double den = 0.0;
        int w = 1;
        for (int i = start; i <= end; i++) {
            num += v[i] * w;
            den += w;
            w++;
        }
        return den == 0.0 ? Double.NaN : (num / den);
    }

    private static double hma(double[] close, int end, int period) {
        if (period <= 1) return Double.NaN;
        int half = period / 2;
        int sqrt = (int) Math.round(Math.sqrt(period));
        if (sqrt < 1) sqrt = 1;

        // Create diff series for last sqrt periods: diff[i] = 2*WMA(half) - WMA(full)
        int diffStart = end - sqrt + 1;
        if (diffStart < 0) return Double.NaN;

        double[] diff = new double[sqrt];
        for (int i = 0; i < sqrt; i++) {
            int idx = diffStart + i;
            double wHalf = wma(close, idx, half);
            double wFull = wma(close, idx, period);
            if (Double.isNaN(wHalf) || Double.isNaN(wFull)) return Double.NaN;
            diff[i] = 2.0 * wHalf - wFull;
        }

        // WMA on diff array
        double num = 0.0;
        double den = 0.0;
        int w = 1;
        for (double d : diff) {
            num += d * w;
            den += w;
            w++;
        }
        return den == 0.0 ? Double.NaN : (num / den);
    }

    private static double ichimokuBaseLine(double[] high, double[] low, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double hh = Double.NEGATIVE_INFINITY;
        double ll = Double.POSITIVE_INFINITY;
        for (int i = start; i <= end; i++) {
            hh = Math.max(hh, high[i]);
            ll = Math.min(ll, low[i]);
        }
        if (!Double.isFinite(hh) || !Double.isFinite(ll)) return Double.NaN;
        return (hh + ll) / 2.0;
    }

    private static double momentum(double[] close, int end, int period) {
        int idx = end - period;
        if (idx < 0) return Double.NaN;
        return close[end] - close[idx];
    }

    private static double rsi(double[] close, int end, int period) {
        if (end < period) return Double.NaN;

        double gain = 0.0;
        double loss = 0.0;
        // seed using first period changes
        for (int i = 1; i <= period; i++) {
            double ch = close[i] - close[i - 1];
            if (ch >= 0) gain += ch;
            else loss -= ch;
        }
        gain /= period;
        loss /= period;

        for (int i = period + 1; i <= end; i++) {
            double ch = close[i] - close[i - 1];
            double g = ch > 0 ? ch : 0.0;
            double l = ch < 0 ? -ch : 0.0;
            gain = (gain * (period - 1) + g) / period;
            loss = (loss * (period - 1) + l) / period;
        }

        if (loss == 0.0) return 100.0;
        double rs = gain / loss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    private record StochKD(double k, double d) {}

    private static StochKD stochastic(double[] high, double[] low, double[] close, int end, int kPeriod, int dPeriod) {
        if (end < kPeriod - 1) return new StochKD(Double.NaN, Double.NaN);

        // K at end
        double kEnd = stochK(high, low, close, end, kPeriod);
        if (Double.isNaN(kEnd)) return new StochKD(Double.NaN, Double.NaN);

        // D = SMA of last dPeriod K values
        if (end < kPeriod - 1 + (dPeriod - 1)) {
            // still compute D if possible
            int start = end - (dPeriod - 1);
            if (start < 0) return new StochKD(kEnd, Double.NaN);
        }

        int startD = end - dPeriod + 1;
        if (startD < 0) return new StochKD(kEnd, Double.NaN);
        double sum = 0.0;
        int count = 0;
        for (int i = startD; i <= end; i++) {
            double k = stochK(high, low, close, i, kPeriod);
            if (Double.isNaN(k)) continue;
            sum += k;
            count++;
        }
        double d = (count == dPeriod) ? (sum / dPeriod) : Double.NaN;
        return new StochKD(kEnd, d);
    }

    private static double stochK(double[] high, double[] low, double[] close, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double hh = Double.NEGATIVE_INFINITY;
        double ll = Double.POSITIVE_INFINITY;
        for (int i = start; i <= end; i++) {
            hh = Math.max(hh, high[i]);
            ll = Math.min(ll, low[i]);
        }
        double denom = hh - ll;
        if (denom == 0.0) return 0.0;
        return 100.0 * (close[end] - ll) / denom;
    }

    private static double cci(double[] high, double[] low, double[] close, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;

        double[] tp = new double[period];
        double sumTp = 0.0;
        for (int i = 0; i < period; i++) {
            int idx = start + i;
            tp[i] = (high[idx] + low[idx] + close[idx]) / 3.0;
            sumTp += tp[i];
        }
        double smaTp = sumTp / period;
        double md = 0.0;
        for (double v : tp) md += Math.abs(v - smaTp);
        md /= period;
        if (md == 0.0) return 0.0;

        double tpEnd = (high[end] + low[end] + close[end]) / 3.0;
        return (tpEnd - smaTp) / (0.015 * md);
    }

    private record AdxPack(double adx, double plusDi, double minusDi) {}

    private static AdxPack adx(double[] high, double[] low, double[] close, int end, int period) {
        // Needs enough bars for smoothing; if not, return NaN
        if (end < period * 2) return new AdxPack(Double.NaN, Double.NaN, Double.NaN);

        // Compute TR, +DM, -DM arrays
        int n = end + 1;
        double[] tr = new double[n];
        double[] plusDm = new double[n];
        double[] minusDm = new double[n];

        tr[0] = 0.0;
        plusDm[0] = 0.0;
        minusDm[0] = 0.0;

        for (int i = 1; i < n; i++) {
            double highLow = high[i] - low[i];
            double highPrevClose = Math.abs(high[i] - close[i - 1]);
            double lowPrevClose = Math.abs(low[i] - close[i - 1]);
            tr[i] = Math.max(highLow, Math.max(highPrevClose, lowPrevClose));

            double upMove = high[i] - high[i - 1];
            double downMove = low[i - 1] - low[i];

            plusDm[i] = (upMove > downMove && upMove > 0) ? upMove : 0.0;
            minusDm[i] = (downMove > upMove && downMove > 0) ? downMove : 0.0;
        }

        // Wilder smoothing initial sums at i=period
        double tr14 = 0.0;
        double plus14 = 0.0;
        double minus14 = 0.0;
        for (int i = 1; i <= period; i++) {
            tr14 += tr[i];
            plus14 += plusDm[i];
            minus14 += minusDm[i];
        }

        double plusDi = 100.0 * (plus14 / tr14);
        double minusDi = 100.0 * (minus14 / tr14);
        double dx = 100.0 * Math.abs(plusDi - minusDi) / (plusDi + minusDi);

        // Seed ADX as average of next 'period' DX values (period+1 .. 2*period)
        double adx = 0.0;
        int dxCount = 0;

        double trSm = tr14;
        double plusSm = plus14;
        double minusSm = minus14;

        for (int i = period + 1; i <= period * 2; i++) {
            trSm = trSm - (trSm / period) + tr[i];
            plusSm = plusSm - (plusSm / period) + plusDm[i];
            minusSm = minusSm - (minusSm / period) + minusDm[i];

            double pdi = 100.0 * (plusSm / trSm);
            double mdi = 100.0 * (minusSm / trSm);
            double dx_i = 100.0 * Math.abs(pdi - mdi) / (pdi + mdi);
            adx += dx_i;
            dxCount++;
        }

        adx = adx / dxCount;

        // Continue smoothing up to end
        for (int i = period * 2 + 1; i <= end; i++) {
            trSm = trSm - (trSm / period) + tr[i];
            plusSm = plusSm - (plusSm / period) + plusDm[i];
            minusSm = minusSm - (minusSm / period) + minusDm[i];

            plusDi = 100.0 * (plusSm / trSm);
            minusDi = 100.0 * (minusSm / trSm);
            dx = 100.0 * Math.abs(plusDi - minusDi) / (plusDi + minusDi);
            adx = (adx * (period - 1) + dx) / period;
        }

        return new AdxPack(adx, plusDi, minusDi);
    }

    private static double awesomeOscillator(double[] high, double[] low, int end) {
        int n = end + 1;
        double[] median = new double[n];
        for (int i = 0; i < n; i++) median[i] = (high[i] + low[i]) / 2.0;
        double sma5 = sma(median, end, 5);
        double sma34 = sma(median, end, 34);
        if (Double.isNaN(sma5) || Double.isNaN(sma34)) return Double.NaN;
        return sma5 - sma34;
    }

    private record MacdPack(double macd, double signal) {}

    private static MacdPack macd(double[] close, int end, int fast, int slow, int signalPeriod) {
        if (end < slow - 1) return new MacdPack(Double.NaN, Double.NaN);

        double alphaFast = 2.0 / (fast + 1.0);
        double alphaSlow = 2.0 / (slow + 1.0);

        // seed EMAs
        double emaFast = sma(close, fast - 1, fast);
        double emaSlow = sma(close, slow - 1, slow);
        if (Double.isNaN(emaFast) || Double.isNaN(emaSlow)) return new MacdPack(Double.NaN, Double.NaN);

        // Bring emaFast up to slow-1 so both aligned
        for (int i = fast; i <= slow - 1; i++) {
            emaFast = alphaFast * close[i] + (1.0 - alphaFast) * emaFast;
        }

        double macd = 0.0;
        double signal = Double.NaN;

        // build MACD series from slow-1..end and EMA it for signal
        double alphaSignal = 2.0 / (signalPeriod + 1.0);
        int macdStart = slow - 1;

        // Seed signal with SMA of first signalPeriod macd values
        if (end < macdStart + signalPeriod - 1) {
            // not enough to compute signal
            for (int i = slow; i <= end; i++) {
                emaFast = alphaFast * close[i] + (1.0 - alphaFast) * emaFast;
                emaSlow = alphaSlow * close[i] + (1.0 - alphaSlow) * emaSlow;
            }
            macd = emaFast - emaSlow;
            return new MacdPack(macd, Double.NaN);
        }

        double signalSeedSum = 0.0;
        // compute first macd at macdStart
        macd = emaFast - emaSlow;
        signalSeedSum += macd;

        for (int i = macdStart + 1; i <= macdStart + signalPeriod - 1; i++) {
            emaFast = alphaFast * close[i] + (1.0 - alphaFast) * emaFast;
            emaSlow = alphaSlow * close[i] + (1.0 - alphaSlow) * emaSlow;
            macd = emaFast - emaSlow;
            signalSeedSum += macd;
        }

        signal = signalSeedSum / signalPeriod;

        for (int i = macdStart + signalPeriod; i <= end; i++) {
            emaFast = alphaFast * close[i] + (1.0 - alphaFast) * emaFast;
            emaSlow = alphaSlow * close[i] + (1.0 - alphaSlow) * emaSlow;
            macd = emaFast - emaSlow;
            signal = alphaSignal * macd + (1.0 - alphaSignal) * signal;
        }

        return new MacdPack(macd, signal);
    }

    private static StochKD stochasticRsi(double[] close, int end, int rsiPeriod, int stochPeriod, int kSmaPeriod) {
        // Need RSI series to compute stochRSI
        if (end < rsiPeriod + stochPeriod) return new StochKD(Double.NaN, Double.NaN);

        // compute RSI values for relevant window up to end
        double[] rsi = new double[end + 1];
        for (int i = 0; i <= end; i++) rsi[i] = Double.NaN;

        // seed RSI at index = rsiPeriod
        double gain = 0.0;
        double loss = 0.0;
        for (int i = 1; i <= rsiPeriod; i++) {
            double ch = close[i] - close[i - 1];
            if (ch >= 0) gain += ch; else loss -= ch;
        }
        gain /= rsiPeriod;
        loss /= rsiPeriod;
        rsi[rsiPeriod] = (loss == 0.0) ? 100.0 : (100.0 - (100.0 / (1.0 + (gain / loss))));

        for (int i = rsiPeriod + 1; i <= end; i++) {
            double ch = close[i] - close[i - 1];
            double g = ch > 0 ? ch : 0.0;
            double l = ch < 0 ? -ch : 0.0;
            gain = (gain * (rsiPeriod - 1) + g) / rsiPeriod;
            loss = (loss * (rsiPeriod - 1) + l) / rsiPeriod;
            rsi[i] = (loss == 0.0) ? 100.0 : (100.0 - (100.0 / (1.0 + (gain / loss))));
        }

        // compute stochRSI K at end
        double kEnd = stochKFromSeries(rsi, end, stochPeriod);

        // D = SMA of last 3 k values (TradingView uses 3,3)
        int dPeriod = 3;
        int start = end - dPeriod + 1;
        if (start < 0) return new StochKD(kEnd, Double.NaN);
        double sum = 0.0;
        int count = 0;
        for (int i = start; i <= end; i++) {
            double k = stochKFromSeries(rsi, i, stochPeriod);
            if (Double.isNaN(k)) continue;
            sum += k;
            count++;
        }
        double d = (count == dPeriod) ? (sum / dPeriod) : Double.NaN;

        // Smooth K with SMA(kSmaPeriod) if requested (kSmaPeriod==3)
        if (kSmaPeriod > 1) {
            int ks = end - kSmaPeriod + 1;
            if (ks >= 0) {
                double kSum = 0.0;
                int kCount = 0;
                for (int i = ks; i <= end; i++) {
                    double k = stochKFromSeries(rsi, i, stochPeriod);
                    if (Double.isNaN(k)) continue;
                    kSum += k;
                    kCount++;
                }
                if (kCount == kSmaPeriod) {
                    kEnd = kSum / kSmaPeriod;
                }
            }
        }

        return new StochKD(kEnd, d);
    }

    private static double stochKFromSeries(double[] series, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = start; i <= end; i++) {
            double v = series[i];
            if (Double.isNaN(v)) return Double.NaN;
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        double denom = max - min;
        if (denom == 0.0) return 0.0;
        return 100.0 * (series[end] - min) / denom;
    }

    private static double williamsR(double[] high, double[] low, double[] close, int end, int period) {
        int start = end - period + 1;
        if (start < 0) return Double.NaN;
        double hh = Double.NEGATIVE_INFINITY;
        double ll = Double.POSITIVE_INFINITY;
        for (int i = start; i <= end; i++) {
            hh = Math.max(hh, high[i]);
            ll = Math.min(ll, low[i]);
        }
        double denom = hh - ll;
        if (denom == 0.0) return 0.0;
        return -100.0 * (hh - close[end]) / denom;
    }

    private record BullBear(double bull, double bear) {}

    private static BullBear bullBearPower(double[] high, double[] low, double[] close, int end, int emaPeriod) {
        double ema = ema(close, end, emaPeriod);
        if (Double.isNaN(ema)) return new BullBear(Double.NaN, Double.NaN);
        double bull = high[end] - ema;
        double bear = low[end] - ema;
        return new BullBear(bull, bear);
    }

    private static double ultimateOscillator(double[] high, double[] low, double[] close, int end, int p1, int p2, int p3) {
        if (end < p3) return Double.NaN;

        double sumBP1 = 0, sumTR1 = 0;
        double sumBP2 = 0, sumTR2 = 0;
        double sumBP3 = 0, sumTR3 = 0;

        for (int i = end - p3 + 1; i <= end; i++) {
            double prevClose = close[i - 1];
            double bp = close[i] - Math.min(low[i], prevClose);
            double tr = Math.max(high[i], prevClose) - Math.min(low[i], prevClose);

            if (i > end - p1) {
                sumBP1 += bp;
                sumTR1 += tr;
            }
            if (i > end - p2) {
                sumBP2 += bp;
                sumTR2 += tr;
            }
            sumBP3 += bp;
            sumTR3 += tr;
        }

        if (sumTR1 == 0 || sumTR2 == 0 || sumTR3 == 0) return Double.NaN;
        double avg1 = sumBP1 / sumTR1;
        double avg2 = sumBP2 / sumTR2;
        double avg3 = sumBP3 / sumTR3;

        return 100.0 * (4 * avg1 + 2 * avg2 + avg3) / 7.0;
    }

    private TvRater() {}
}

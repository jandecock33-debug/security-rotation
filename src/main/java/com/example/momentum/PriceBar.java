
package com.example.momentum;

import java.time.LocalDate;

/**
 * Single daily bar with OHLCV.
 */
public record PriceBar(LocalDate date,
                       double open,
                       double high,
                       double low,
                       double close,
                       double volume) {}

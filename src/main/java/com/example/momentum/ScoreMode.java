package com.example.momentum;

public enum ScoreMode {
    RS_COMBINED,   // 3/6/12-month combined momentum
    RETURN_6M,     // last 6 months percentage return

    /**
     * TradingView-style technical score using oscillator + moving-average ratings.
     * Computes Daily / Weekly / Monthly overall scores, then ranks lexicographically:
     * Monthly desc, then Weekly desc, then Daily desc.
     */
    TV_TECHNICAL
}

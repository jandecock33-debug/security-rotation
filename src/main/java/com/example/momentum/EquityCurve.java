
package com.example.momentum;

import java.time.LocalDate;
import java.util.List;

public record  EquityCurve(List<LocalDate> dates, List<Double> equity) {}

package com.trajectoryfit.service;

import com.trajectoryfit.risk.ProteinNormalizer;
import com.trajectoryfit.risk.config.RiskConfig;
import com.trajectoryfit.risk.model.DailyLog;
import com.trajectoryfit.risk.model.ProteinLevel;
import com.trajectoryfit.risk.model.TrainingType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Builds the time series the dashboard charts: weight (actual and a trailing smoothed
 * line), daily protein level, and training per day, plus a couple of headline stats.
 * Weights are emitted in pounds — this is a presentation-layer service, so the metric
 * values from storage are converted on the way out.
 */
@Service
public class TrendsService {

    /** Trailing window (days) used both for the smoothed weight line and each day's protein target. */
    private static final int SMOOTHING_WINDOW = 7;

    private final LogWindowAssembler assembler;
    private final RiskConfig riskConfig;

    public TrendsService(LogWindowAssembler assembler, RiskConfig riskConfig) {
        this.assembler = assembler;
        this.riskConfig = riskConfig;
    }

    /** One day on the trend charts. Nulls mean nothing was logged for that signal that day. */
    public record TrendPoint(LocalDate date, Double weightLb, Double weightSmoothedLb,
                             String proteinLevel, String training) {
    }

    public record TrendsResult(int rangeDays, LocalDate from, LocalDate to, List<TrendPoint> points,
                               Double avgProteinAdherencePct, long resistanceSessionsLast7Days) {
    }

    public TrendsResult compute(UUID userId, LocalDate anchor, int rangeDays) {
        LocalDate from = anchor.minusDays(rangeDays - 1L);
        List<DailyLog> days = assembler.assembleRange(userId, from, anchor);

        // Extract per-day weights (lb) for smoothing.
        Double[] weightLb = new Double[days.size()];
        for (int i = 0; i < days.size(); i++) {
            weightLb[i] = days.get(i).weightLb();
        }

        List<TrendPoint> points = new ArrayList<>(days.size());
        int proteinDays = 0;
        int adherentDays = 0;

        for (int i = 0; i < days.size(); i++) {
            DailyLog day = days.get(i);
            double avgWeightLb = trailingAvgWeight(weightLb, i);

            ProteinLevel level = ProteinNormalizer.normalize(day.protein(), avgWeightLb, riskConfig);
            if (level != null) {
                proteinDays++;
                if (level != ProteinLevel.LOW) {
                    adherentDays++;
                }
            }

            Double smoothed = weightLb[i] != null ? round1(avgWeightLb) : null;
            String training = day.trainingType() == null ? null : day.trainingType().name().toLowerCase();

            points.add(new TrendPoint(
                    day.date(),
                    weightLb[i] != null ? round1(weightLb[i]) : null,
                    smoothed,
                    level == null ? null : level.name().toLowerCase(),
                    training));
        }

        Double avgAdherencePct = proteinDays == 0 ? null : round1(100.0 * adherentDays / proteinDays);
        long resistanceLast7 = days.stream()
                .skip(Math.max(0, days.size() - 7))
                .filter(d -> d.trainingType() == TrainingType.RESISTANCE)
                .count();

        return new TrendsResult(rangeDays, from, anchor, points, avgAdherencePct, resistanceLast7);
    }

    /** Average of logged weights within the trailing {@link #SMOOTHING_WINDOW} days ending at index i. */
    private double trailingAvgWeight(Double[] weightLb, int i) {
        double sum = 0;
        int count = 0;
        for (int j = Math.max(0, i - (SMOOTHING_WINDOW - 1)); j <= i; j++) {
            if (weightLb[j] != null) {
                sum += weightLb[j];
                count++;
            }
        }
        if (count > 0) {
            return sum / count;
        }
        return riskConfig.getDefaultBodyweightLb();
    }

    private static double round1(double n) {
        return Math.round(n * 10.0) / 10.0;
    }
}

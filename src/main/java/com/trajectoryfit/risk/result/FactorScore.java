package com.trajectoryfit.risk.result;

/**
 * One factor's contribution to the overall score, kept as its own sub-score plus a
 * human-readable status. Emitting per-factor sub-scores (rather than only a single
 * number) is what lets the app explain <em>why</em> someone is at risk — the actual product.
 *
 * @param factor  which contributing factor this is
 * @param points  the factor's contribution to the total (can be negative, e.g. a
 *                body-comp reward); not yet clamped into the overall 0–100 range
 * @param status  short status label shown on the factor row (e.g. "On track",
 *                "Below target", "1x this week", "No data")
 * @param detail  optional underlying number behind the status (loss-rate %, protein
 *                adherence fraction, or resistance-session count), or {@code null}
 */
public record FactorScore(Factor factor, double points, String status, Double detail) {

    public static FactorScore of(Factor factor, double points, String status) {
        return new FactorScore(factor, points, status, null);
    }

    public static FactorScore of(Factor factor, double points, String status, Double detail) {
        return new FactorScore(factor, points, status, detail);
    }
}

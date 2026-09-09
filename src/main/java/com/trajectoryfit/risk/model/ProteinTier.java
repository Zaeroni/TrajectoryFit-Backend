package com.trajectoryfit.risk.model;

/**
 * Which UI logging tier a day's protein entry came from. All three normalize to a
 * single {@link ProteinLevel} so no tier is a second-class citizen in scoring — this
 * is a deliberate product decision (see design rationale: "Why tiered logging").
 */
public enum ProteinTier {
    /** One Low/Good/High tap for the whole day, no meal breakdown. */
    QUICK,
    /** Per-meal Low/Good/High ratings, no gram math. */
    ESTIMATE,
    /** Exact grams per meal, compared against a per-kg bodyweight target. */
    PRECISE
}

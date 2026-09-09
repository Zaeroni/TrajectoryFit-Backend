package com.trajectoryfit.risk.model;

/**
 * The kind of training logged for a day. A {@code null} TrainingType on a
 * {@link DailyLog} means the day was never logged at all — which is treated very
 * differently from {@link #REST} (an explicitly logged rest day). Only
 * {@link #RESISTANCE} counts toward the protective session count; cardio does not.
 */
public enum TrainingType {
    RESISTANCE,
    CARDIO,
    /** An explicitly logged rest day. Counts as "training was logged", but is zero sessions. */
    REST
}

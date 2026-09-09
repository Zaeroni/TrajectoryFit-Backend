package com.trajectoryfit.risk.model;

/**
 * A day's protein intake, normalized to a coarse three-level scale regardless of
 * which logging tier (Quick / Estimate / Precise) produced it. See
 * {@link com.trajectoryfit.risk.ProteinNormalizer} for how each tier collapses to this.
 */
public enum ProteinLevel {
    LOW(0),
    GOOD(1),
    HIGH(2);

    private final int ordinalValue;

    ProteinLevel(int ordinalValue) {
        this.ordinalValue = ordinalValue;
    }

    /** Numeric weight used when averaging per-meal ratings in the Estimate tier. */
    public int numeric() {
        return ordinalValue;
    }
}

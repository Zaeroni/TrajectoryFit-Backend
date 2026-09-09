package com.trajectoryfit.risk.result;

/** The three overall risk bands a score falls into. */
public enum RiskBand {
    LOW("Low"),
    MODERATE("Moderate"),
    HIGH("High");

    private final String label;

    RiskBand(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

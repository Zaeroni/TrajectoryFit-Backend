package com.trajectoryfit.risk.result;

/** The four contributing factors that roll up into the overall score. */
public enum Factor {
    WEIGHT("Weight loss rate"),
    PROTEIN("Protein intake"),
    TRAINING("Training frequency"),
    BODY_COMP("Body composition trend");

    private final String label;

    Factor(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}

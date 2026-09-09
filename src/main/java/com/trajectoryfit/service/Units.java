package com.trajectoryfit.service;

/**
 * Imperial ↔ metric conversions. The database and scoring core have their own unit
 * conventions (metric storage; the scoring core works in pounds); the API faces imperial,
 * so all conversion for request/response bodies funnels through here.
 */
public final class Units {

    public static final double LB_PER_KG = 2.2046;
    public static final double CM_PER_IN = 2.54;

    private Units() {
    }

    public static double lbToKg(double lb) {
        return lb / LB_PER_KG;
    }

    public static double kgToLb(double kg) {
        return kg * LB_PER_KG;
    }

    public static double inToCm(double inches) {
        return inches * CM_PER_IN;
    }

    public static double cmToIn(double cm) {
        return cm / CM_PER_IN;
    }

    public static Double kgToLb(Double kg) {
        return kg == null ? null : kgToLb((double) kg);
    }

    public static Double cmToIn(Double cm) {
        return cm == null ? null : cmToIn((double) cm);
    }

    public static double round1(double n) {
        return Math.round(n * 10.0) / 10.0;
    }

    public static Double round1(Double n) {
        return n == null ? null : round1((double) n);
    }
}

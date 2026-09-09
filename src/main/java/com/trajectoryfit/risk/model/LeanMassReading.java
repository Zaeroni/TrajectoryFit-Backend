package com.trajectoryfit.risk.model;

import java.time.LocalDate;

/**
 * One lean-mass reading from a body-composition scan (InBody, smart scale, DEXA, etc.),
 * in pounds. Body composition is a confirming signal, not a gate: the risk model only
 * uses it when two or more readings exist anywhere in the user's history, and its
 * absence never penalizes a user.
 */
public record LeanMassReading(LocalDate date, double leanMassLb) {
}

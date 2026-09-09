package com.trajectoryfit.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A body-composition reading. Both measurements are optional but at least one must be
 * present (enforced in the controller). Lean mass is in pounds (20–500), body fat in
 * percent (3–70).
 */
public record BodyCompRequest(
        @NotNull UUID userId,
        @NotNull LocalDate loggedOn,
        @DecimalMin("3") @DecimalMax("70") Double bodyFatPct,
        @DecimalMin("20") @DecimalMax("500") Double leanMassLb,
        @Pattern(regexp = "gym_scan|smart_scale|paid_scan",
                message = "must be one of: gym_scan, smart_scale, paid_scan") String source
) {
}

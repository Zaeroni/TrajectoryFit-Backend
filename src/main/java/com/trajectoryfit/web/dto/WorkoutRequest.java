package com.trajectoryfit.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * A training entry for a day. Only {@code resistance} counts toward the protective session
 * count; {@code cardio} and {@code rest} are logged but do not. Logging an explicit
 * {@code rest} day still counts as "training was logged".
 */
public record WorkoutRequest(
        @NotNull UUID userId,
        @NotNull LocalDate loggedOn,
        @NotNull @Pattern(regexp = "resistance|cardio|rest",
                message = "must be one of: resistance, cardio, rest") String type,
        @Min(0) @Max(600) Integer durationMin,
        List<String> muscles,
        @Pattern(regexp = "light|moderate|hard",
                message = "must be one of: light, moderate, hard") String effort,
        String cardioType
) {
}

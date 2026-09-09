package com.trajectoryfit.web.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

/** A weight reading in pounds for a specific day (sanity bounds 60–700 lb). */
public record WeighInRequest(
        @NotNull UUID userId,
        @NotNull LocalDate loggedOn,
        @NotNull @DecimalMin("60") @DecimalMax("700") Double weightLb
) {
}

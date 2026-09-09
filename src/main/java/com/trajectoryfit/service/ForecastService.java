package com.trajectoryfit.service;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Placeholder for the Phase-2 forecasting service. Forecasting is the first ML application
 * that doesn't require labeled outcome data — it extrapolates a user's own trajectory from
 * their own history — but the model itself lives in a separate service that doesn't exist
 * yet. This stub establishes the endpoint contract so the frontend and API can be built
 * against a stable shape before the model arrives.
 */
@Service
public class ForecastService {

    /** A single projected point the model will eventually return (weight in pounds). */
    public record ForecastPoint(LocalDate date, Double projectedWeightLb, Double lowerBoundLb, Double upperBoundLb) {
    }

    public record ForecastResult(boolean modelAvailable, String status, String note,
                                 int horizonDays, List<ForecastPoint> projection) {
    }

    public ForecastResult forecast(UUID userId, int horizonDays) {
        return new ForecastResult(
                false,
                "not_implemented",
                "Forecasting is planned for Phase 2 as a separate model service. This endpoint "
                        + "returns the stable response contract so clients can integrate ahead of the model; "
                        + "no projection is produced yet.",
                horizonDays,
                List.of());
    }
}

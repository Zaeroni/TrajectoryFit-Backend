package com.trajectoryfit.web.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * A day's protein entry in any of the three tiers. Exactly one tier's payload is expected:
 * <ul>
 *   <li>{@code quick}: set {@code level} (low/good/high) for the whole day.</li>
 *   <li>{@code estimate}: set {@code mealLevels}, e.g. {@code {"breakfast":"good","dinner":"low"}}.</li>
 *   <li>{@code precise}: set {@code mealGrams}, e.g. {@code {"breakfast":40,"dinner":35}}.</li>
 * </ul>
 * The tier/payload consistency and gram bounds (0–500) are checked in the controller.
 */
public record NutritionRequest(
        @NotNull UUID userId,
        @NotNull LocalDate loggedOn,
        @NotNull @Pattern(regexp = "quick|estimate|precise",
                message = "must be one of: quick, estimate, precise") String tier,
        @Pattern(regexp = "low|good|high", message = "must be one of: low, good, high") String level,
        Map<String, String> mealLevels,
        Map<String, Double> mealGrams
) {
}

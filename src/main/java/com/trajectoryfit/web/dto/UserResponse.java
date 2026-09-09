package com.trajectoryfit.web.dto;

import com.trajectoryfit.domain.UserEntity;
import com.trajectoryfit.service.Units;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/** User representation returned by the API, with measurements converted back to imperial. */
public record UserResponse(
        UUID id,
        String name,
        Double heightIn,
        Double goalWeightLb,
        String medication,
        String loggingPreference,
        List<String> bodyCompAccess,
        String archetype,
        String expectedBand,
        Instant createdAt
) {
    public static UserResponse from(UserEntity u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                Units.round1(Units.cmToIn(u.getHeightCm())),
                Units.round1(Units.kgToLb(u.getGoalWeightKg())),
                u.getMedication(),
                u.getLoggingPreference(),
                u.getBodyCompAccess() == null ? List.of() : Arrays.asList(u.getBodyCompAccess()),
                u.getArchetype(),
                u.getExpectedBand(),
                u.getCreatedAt());
    }
}

package com.trajectoryfit.web.dto;

import com.trajectoryfit.domain.TrainingLogEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public record WorkoutResponse(
        Long id, LocalDate loggedOn, String type, Integer durationMin,
        List<String> muscles, String effort, String cardioType) {

    public static WorkoutResponse from(TrainingLogEntity t) {
        return new WorkoutResponse(
                t.getId(),
                t.getLoggedOn(),
                t.getType(),
                t.getDurationMin(),
                t.getMuscles() == null ? List.of() : Arrays.asList(t.getMuscles()),
                t.getEffort(),
                t.getCardioType());
    }
}

package com.trajectoryfit.web.dto;

import com.trajectoryfit.domain.BodyCompLogEntity;
import com.trajectoryfit.service.Units;

import java.time.LocalDate;

public record BodyCompResponse(Long id, LocalDate loggedOn, Double bodyFatPct, Double leanMassLb, String source) {
    public static BodyCompResponse from(BodyCompLogEntity b) {
        return new BodyCompResponse(
                b.getId(),
                b.getLoggedOn(),
                Units.round1(b.getBodyFatPct()),
                Units.round1(Units.kgToLb(b.getLeanMassKg())),
                b.getSource());
    }
}

package com.trajectoryfit.web.dto;

import com.trajectoryfit.domain.WeightLogEntity;
import com.trajectoryfit.service.Units;

import java.time.LocalDate;

public record WeighInResponse(Long id, LocalDate loggedOn, Double weightLb) {
    public static WeighInResponse from(WeightLogEntity w) {
        return new WeighInResponse(w.getId(), w.getLoggedOn(), Units.round1(Units.kgToLb(w.getWeightKg())));
    }
}

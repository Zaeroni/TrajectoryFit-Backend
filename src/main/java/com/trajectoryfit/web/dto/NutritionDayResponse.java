package com.trajectoryfit.web.dto;

import com.trajectoryfit.domain.NutritionLogEntity;

import java.time.LocalDate;
import java.util.List;

/** A day's protein logging: its tier and the per-meal (or whole-day) entries stored. */
public record NutritionDayResponse(LocalDate loggedOn, String tier, List<Meal> meals) {

    public record Meal(String meal, String level, Double grams) {
        public static Meal from(NutritionLogEntity e) {
            return new Meal(e.getMeal(), e.getLevel(), e.getGrams());
        }
    }
}

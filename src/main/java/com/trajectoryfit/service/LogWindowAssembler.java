package com.trajectoryfit.service;

import com.trajectoryfit.domain.BodyCompLogEntity;
import com.trajectoryfit.domain.NutritionLogEntity;
import com.trajectoryfit.domain.TrainingLogEntity;
import com.trajectoryfit.domain.WeightLogEntity;
import com.trajectoryfit.repo.BodyCompLogRepository;
import com.trajectoryfit.repo.NutritionLogRepository;
import com.trajectoryfit.repo.TrainingLogRepository;
import com.trajectoryfit.repo.WeightLogRepository;
import com.trajectoryfit.risk.model.DailyLog;
import com.trajectoryfit.risk.model.LeanMassReading;
import com.trajectoryfit.risk.model.ProteinLevel;
import com.trajectoryfit.risk.model.ProteinLog;
import com.trajectoryfit.risk.model.TrainingType;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Assembles a user's stored logs into the framework-free inputs the {@link
 * com.trajectoryfit.risk.RiskService} consumes. This is the one place metric storage
 * meets the pounds/grams the scoring rules use — the conversion boundary lives here so the
 * database stays metric and the scoring core stays a faithful port of the reference.
 */
@Service
public class LogWindowAssembler {

    /** Pounds per kilogram. Weights and lean mass are stored in kg, scored in lb. */
    public static final double LB_PER_KG = 2.2046;

    /** Meal slot order used by the Estimate/Precise tiers. */
    private static final List<String> MEAL_ORDER = List.of("breakfast", "lunch", "dinner", "snacks");

    private final WeightLogRepository weightRepo;
    private final NutritionLogRepository nutritionRepo;
    private final TrainingLogRepository trainingRepo;
    private final BodyCompLogRepository bodyCompRepo;

    public LogWindowAssembler(WeightLogRepository weightRepo, NutritionLogRepository nutritionRepo,
                              TrainingLogRepository trainingRepo, BodyCompLogRepository bodyCompRepo) {
        this.weightRepo = weightRepo;
        this.nutritionRepo = nutritionRepo;
        this.trainingRepo = trainingRepo;
        this.bodyCompRepo = bodyCompRepo;
    }

    /** The trailing 7-day window ending (inclusive) on {@code anchor}, oldest day first. */
    public List<DailyLog> assembleWindow(UUID userId, LocalDate anchor) {
        return assembleRange(userId, anchor.minusDays(6), anchor);
    }

    /** A {@link DailyLog} per calendar day across the inclusive range, oldest first. */
    public List<DailyLog> assembleRange(UUID userId, LocalDate from, LocalDate to) {
        Map<LocalDate, WeightLogEntity> weights = weightRepo
                .findByUserIdAndLoggedOnBetweenOrderByLoggedOnAsc(userId, from, to).stream()
                .collect(Collectors.toMap(WeightLogEntity::getLoggedOn, w -> w, (a, b) -> a));

        Map<LocalDate, List<NutritionLogEntity>> nutrition = nutritionRepo
                .findByUserIdAndLoggedOnBetweenOrderByLoggedOnAsc(userId, from, to).stream()
                .collect(Collectors.groupingBy(NutritionLogEntity::getLoggedOn));

        Map<LocalDate, TrainingLogEntity> training = trainingRepo
                .findByUserIdAndLoggedOnBetweenOrderByLoggedOnAsc(userId, from, to).stream()
                .collect(Collectors.toMap(TrainingLogEntity::getLoggedOn, t -> t, (a, b) -> a));

        List<DailyLog> window = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            DailyLog.Builder day = DailyLog.builder(date);

            WeightLogEntity w = weights.get(date);
            if (w != null) {
                day.weightLb(w.getWeightKg() * LB_PER_KG);
            }

            ProteinLog protein = toProteinLog(nutrition.get(date));
            if (protein != null) {
                day.protein(protein);
            }

            TrainingLogEntity t = training.get(date);
            if (t != null) {
                day.training(toTrainingType(t.getType()));
            }

            window.add(day.build());
        }
        return window;
    }

    /** All of a user's lean-mass readings, oldest first, in pounds. */
    public List<LeanMassReading> assembleLeanMassHistory(UUID userId) {
        List<LeanMassReading> readings = new ArrayList<>();
        for (BodyCompLogEntity b : bodyCompRepo.findByUserIdOrderByLoggedOnAsc(userId)) {
            if (b.getLeanMassKg() != null) {
                readings.add(new LeanMassReading(b.getLoggedOn(), b.getLeanMassKg() * LB_PER_KG));
            }
        }
        return readings;
    }

    // ------------------------------------------------------------------

    /** Reconstruct a day's {@link ProteinLog} from its stored rows, precise &gt; estimate &gt; quick. */
    private ProteinLog toProteinLog(List<NutritionLogEntity> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        boolean hasPrecise = rows.stream().anyMatch(r -> "precise".equals(r.getTier()));
        if (hasPrecise) {
            Double[] grams = new Double[MEAL_ORDER.size()];
            for (NutritionLogEntity r : rows) {
                if ("precise".equals(r.getTier())) {
                    int idx = MEAL_ORDER.indexOf(r.getMeal());
                    if (idx >= 0) {
                        grams[idx] = r.getGrams();
                    }
                }
            }
            return ProteinLog.precise(grams);
        }
        boolean hasEstimate = rows.stream().anyMatch(r -> "estimate".equals(r.getTier()));
        if (hasEstimate) {
            ProteinLevel[] levels = new ProteinLevel[MEAL_ORDER.size()];
            for (NutritionLogEntity r : rows) {
                if ("estimate".equals(r.getTier())) {
                    int idx = MEAL_ORDER.indexOf(r.getMeal());
                    if (idx >= 0) {
                        levels[idx] = toLevel(r.getLevel());
                    }
                }
            }
            return ProteinLog.estimate(levels);
        }
        // Quick: single whole-day rating.
        return rows.stream()
                .filter(r -> "quick".equals(r.getTier()))
                .findFirst()
                .map(r -> ProteinLog.quick(toLevel(r.getLevel())))
                .orElse(null);
    }

    private ProteinLevel toLevel(String level) {
        if (level == null) {
            return null;
        }
        return switch (level) {
            case "low" -> ProteinLevel.LOW;
            case "good" -> ProteinLevel.GOOD;
            case "high" -> ProteinLevel.HIGH;
            default -> null;
        };
    }

    private TrainingType toTrainingType(String type) {
        return switch (type) {
            case "resistance" -> TrainingType.RESISTANCE;
            case "cardio" -> TrainingType.CARDIO;
            case "rest" -> TrainingType.REST;
            default -> null;
        };
    }
}

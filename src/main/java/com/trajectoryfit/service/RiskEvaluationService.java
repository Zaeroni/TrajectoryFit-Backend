package com.trajectoryfit.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trajectoryfit.domain.RiskScoreEntity;
import com.trajectoryfit.repo.RiskScoreRepository;
import com.trajectoryfit.risk.RiskService;
import com.trajectoryfit.risk.model.DailyLog;
import com.trajectoryfit.risk.model.LeanMassReading;
import com.trajectoryfit.risk.result.FactorScore;
import com.trajectoryfit.risk.result.RiskAssessment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Orchestrates a full risk evaluation for a user: assembles their recent windows, runs the
 * rules engine, checks for sustained risk across the last three weeks, and persists a score
 * snapshot so the trajectory can be charted over time.
 */
@Service
public class RiskEvaluationService {

    private final RiskService riskService;
    private final LogWindowAssembler assembler;
    private final RiskScoreRepository riskScoreRepo;
    private final ObjectMapper objectMapper;

    public RiskEvaluationService(RiskService riskService, LogWindowAssembler assembler,
                                 RiskScoreRepository riskScoreRepo, ObjectMapper objectMapper) {
        this.riskService = riskService;
        this.assembler = assembler;
        this.riskScoreRepo = riskScoreRepo;
        this.objectMapper = objectMapper;
    }

    /** The assessment for the current week plus whether risk has been sustained. */
    public record RiskEvaluation(RiskAssessment assessment, boolean sustainedRisk, LocalDate anchor) {
    }

    public RiskEvaluation evaluate(UUID userId, LocalDate anchor) {
        List<DailyLog> window = assembler.assembleWindow(userId, anchor);
        List<LeanMassReading> leanMass = assembler.assembleLeanMassHistory(userId);

        RiskAssessment assessment = riskService.scoreWeek(window, leanMass);

        boolean sustained = checkSustainedRisk(userId, anchor);

        if (assessment.hasData()) {
            persistSnapshot(userId, anchor, assessment);
        }
        return new RiskEvaluation(assessment, sustained, anchor);
    }

    /**
     * Sustained-risk check across this week and the two prior weeks. Matching the reference,
     * the weekly scores used here are computed <em>without</em> the body-composition modifier
     * (weight + protein + training only) — body comp sharpens the live score, not this rolling
     * pattern check.
     */
    private boolean checkSustainedRisk(UUID userId, LocalDate anchor) {
        int weeks = riskService.config().getSustainedRiskWeeks();
        List<RiskAssessment> weekly = new java.util.ArrayList<>();
        for (int w = 0; w < weeks; w++) {
            List<DailyLog> window = assembler.assembleWindow(userId, anchor.minusWeeks(w));
            weekly.add(riskService.scoreWeek(window, List.of()));
        }
        return riskService.isSustainedRisk(weekly);
    }

    private void persistSnapshot(UUID userId, LocalDate anchor, RiskAssessment a) {
        RiskScoreEntity entity = riskScoreRepo.findByUserIdAndComputedFor(userId, anchor)
                .orElseGet(RiskScoreEntity::new);
        entity.setUserId(userId);
        entity.setComputedFor(anchor);
        entity.setScore(a.score());
        entity.setBand(a.band().label());
        entity.setFactorsJson(factorsJson(a));
        riskScoreRepo.save(entity);
    }

    private String factorsJson(RiskAssessment a) {
        Map<String, Object> root = new LinkedHashMap<>();
        for (FactorScore f : a.factors()) {
            Map<String, Object> node = new LinkedHashMap<>();
            node.put("points", round1(f.points()));
            node.put("status", f.status());
            if (f.detail() != null) {
                node.put("detail", f.detail());
            }
            root.put(f.factor().name().toLowerCase(), node);
        }
        if (a.biggestDriver() != null) {
            root.put("biggestDriver", a.biggestDriver().name().toLowerCase());
        }
        root.put("partialData", a.partialData());
        try {
            return objectMapper.writeValueAsString(root);
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private static double round1(double n) {
        return Math.round(n * 10.0) / 10.0;
    }
}

package com.trajectoryfit.web.dto;

import com.trajectoryfit.risk.result.FactorScore;
import com.trajectoryfit.risk.result.RiskAssessment;
import com.trajectoryfit.service.RiskEvaluationService.RiskEvaluation;

import java.time.LocalDate;
import java.util.List;

/**
 * The {@code /risk} response: the overall score and band, the per-factor sub-scores that
 * explain it, the biggest driver (if any is worth naming), the partial-data confidence
 * flag, and the sustained-risk note.
 */
public record RiskResponse(
        boolean hasData,
        Integer score,
        String band,
        String summary,
        boolean partialData,
        String biggestDriver,
        boolean sustainedRisk,
        String sustainedRiskNote,
        LocalDate anchor,
        List<FactorDto> factors
) {
    public record FactorDto(String factor, String label, double points, String status, Double detail) {
        static FactorDto from(FactorScore f) {
            return new FactorDto(
                    f.factor().name().toLowerCase(),
                    f.factor().label(),
                    Math.round(f.points() * 10.0) / 10.0,
                    f.status(),
                    f.detail());
        }
    }

    public static RiskResponse from(RiskEvaluation eval, int sustainedWeeks) {
        RiskAssessment a = eval.assessment();
        String note = eval.sustainedRisk()
                ? "Your risk has been elevated for " + sustainedWeeks
                    + " weeks straight. Worth mentioning at your next appointment."
                : null;
        return new RiskResponse(
                a.hasData(),
                a.score(),
                a.band() == null ? null : a.band().label(),
                a.summary(),
                a.partialData(),
                a.biggestDriver() == null ? null : a.biggestDriver().name().toLowerCase(),
                eval.sustainedRisk(),
                note,
                eval.anchor(),
                a.factors().stream().map(FactorDto::from).toList());
    }
}

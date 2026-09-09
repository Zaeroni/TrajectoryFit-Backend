package com.trajectoryfit.web;

import com.trajectoryfit.repo.UserRepository;
import com.trajectoryfit.risk.config.RiskConfig;
import com.trajectoryfit.service.RiskEvaluationService;
import com.trajectoryfit.service.RiskEvaluationService.RiskEvaluation;
import com.trajectoryfit.web.dto.RiskResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/**
 * The heart of the app: runs the rules engine over a user's recent windows and returns the
 * score, band, per-factor sub-scores, and sustained-risk note.
 */
@RestController
public class RiskController {

    private final RiskEvaluationService evaluationService;
    private final RiskConfig riskConfig;
    private final UserRepository users;

    public RiskController(RiskEvaluationService evaluationService, RiskConfig riskConfig, UserRepository users) {
        this.evaluationService = evaluationService;
        this.riskConfig = riskConfig;
        this.users = users;
    }

    /**
     * @param id   the user
     * @param date optional "today" anchor (ISO date); defaults to the current date. Useful for
     *             previewing a specific week, matching the prototype's test-anchor feature.
     */
    @GetMapping("/api/users/{id}/risk")
    public RiskResponse risk(@PathVariable UUID id,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!users.existsById(id)) {
            throw new NotFoundException("User " + id + " not found");
        }
        LocalDate anchor = date != null ? date : LocalDate.now();
        RiskEvaluation eval = evaluationService.evaluate(id, anchor);
        return RiskResponse.from(eval, riskConfig.getSustainedRiskWeeks());
    }
}

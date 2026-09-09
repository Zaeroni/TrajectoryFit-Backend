package com.trajectoryfit.risk;

import com.trajectoryfit.risk.archetype.Archetype;
import com.trajectoryfit.risk.archetype.ArchetypeFixtures;
import com.trajectoryfit.risk.config.RiskConfig;
import com.trajectoryfit.risk.result.Factor;
import com.trajectoryfit.risk.result.RiskAssessment;
import com.trajectoryfit.risk.result.RiskBand;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the ported scoring logic to the validated JavaScript reference. Every archetype
 * must land in its documented band; the two load-bearing cases (cardio_only, brand_new)
 * carry extra invariants; and the exact scores are pinned as a regression guard.
 */
class RiskServiceArchetypeTest {

    private final RiskService service = new RiskService(RiskConfig.defaults());

    private RiskAssessment score(Archetype a) {
        ArchetypeFixtures.Case c = ArchetypeFixtures.build(a);
        return service.scoreWeek(c.window(), c.leanMass());
    }

    @Test
    void everyArchetypeLandsInItsExpectedBand() {
        for (Archetype a : Archetype.values()) {
            RiskAssessment r = score(a);
            assertEquals(a.expectedBand(), r.band(), a.key() + " landed in the wrong band");
        }
    }

    @Test
    void scoresMatchTheReferenceExactly() {
        Map<Archetype, Integer> expected = Map.of(
                Archetype.CRASH_DIETER, 100,
                Archetype.DOING_IT_RIGHT, 6,
                Archetype.CARDIO_ONLY, 56,
                Archetype.SLOW_AND_STEADY, 0,
                Archetype.BRAND_NEW, 0);
        expected.forEach((archetype, expectedScore) ->
                assertEquals(expectedScore, score(archetype).score(), archetype.key() + " score drifted from reference"));
    }

    @Test
    void cardioOnlyIsFlaggedWithTrainingAsTheDriver() {
        RiskAssessment r = score(Archetype.CARDIO_ONLY);
        assertEquals(RiskBand.MODERATE, r.band());
        assertEquals(Factor.TRAINING, r.biggestDriver(),
                "cardio without resistance must name training as the driver");
    }

    @Test
    void brandNewIsPartialAndNeverPenalizedForMissingData() {
        RiskAssessment r = score(Archetype.BRAND_NEW);
        assertEquals(RiskBand.LOW, r.band());
        assertTrue(r.partialData(), "sparse week must be flagged as partial data");
        assertEquals("No data", r.training().status(), "unlogged training must read as No data");
        assertEquals(0.0, r.training().points(), "unlogged training must not be penalized");
    }

    @Test
    void doingItRightScoresLowWithoutAlarm() {
        RiskAssessment r = score(Archetype.DOING_IT_RIGHT);
        assertEquals(RiskBand.LOW, r.band());
        assertFalse(r.partialData(), "a fully logged week should not be flagged partial");
        // With the biggest-driver threshold at 10, a near-perfect week's tiny protein dip
        // (~5.7 points) is below the bar, so no driver is named at all.
        assertNull(r.biggestDriver(), "a near-perfect week should not name a driver");
    }
}

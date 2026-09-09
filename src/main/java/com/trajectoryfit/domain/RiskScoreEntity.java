package com.trajectoryfit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A stored risk-score snapshot. Persisting scores as a time series (rather than only
 * computing on demand) is what lets a user's trajectory be charted over time. The
 * contributing per-factor detail is kept as JSONB in {@link #factorsJson}.
 */
@Entity
@Table(name = "risk_scores")
public class RiskScoreEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "computed_for", nullable = false)
    private LocalDate computedFor;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private String band;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "factors", columnDefinition = "jsonb", nullable = false)
    private String factorsJson = "{}";

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getComputedFor() { return computedFor; }
    public void setComputedFor(LocalDate computedFor) { this.computedFor = computedFor; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public String getBand() { return band; }
    public void setBand(String band) { this.band = band; }

    public String getFactorsJson() { return factorsJson; }
    public void setFactorsJson(String factorsJson) { this.factorsJson = factorsJson; }

    public Instant getCreatedAt() { return createdAt; }
}

package com.trajectoryfit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

/** A TrajectoryFit user — the center of the star schema. */
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "height_cm")
    private Double heightCm;

    @Column(name = "goal_weight_kg")
    private Double goalWeightKg;

    @Column(nullable = false)
    private String medication = "none";

    @Column(name = "logging_preference", nullable = false)
    private String loggingPreference = "quick";

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "body_comp_access", columnDefinition = "text[]", nullable = false)
    private String[] bodyCompAccess = new String[0];

    /** Archetype this synthetic user was generated from; null for real users. */
    @Column
    private String archetype;

    /** Expected risk band for the seeded archetype; null for real users. */
    @Column(name = "expected_band")
    private String expectedBand;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getHeightCm() { return heightCm; }
    public void setHeightCm(Double heightCm) { this.heightCm = heightCm; }

    public Double getGoalWeightKg() { return goalWeightKg; }
    public void setGoalWeightKg(Double goalWeightKg) { this.goalWeightKg = goalWeightKg; }

    public String getMedication() { return medication; }
    public void setMedication(String medication) { this.medication = medication; }

    public String getLoggingPreference() { return loggingPreference; }
    public void setLoggingPreference(String loggingPreference) { this.loggingPreference = loggingPreference; }

    public String[] getBodyCompAccess() { return bodyCompAccess; }
    public void setBodyCompAccess(String[] bodyCompAccess) {
        this.bodyCompAccess = bodyCompAccess == null ? new String[0] : bodyCompAccess;
    }

    public String getArchetype() { return archetype; }
    public void setArchetype(String archetype) { this.archetype = archetype; }

    public String getExpectedBand() { return expectedBand; }
    public void setExpectedBand(String expectedBand) { this.expectedBand = expectedBand; }

    public Instant getCreatedAt() { return createdAt; }
}

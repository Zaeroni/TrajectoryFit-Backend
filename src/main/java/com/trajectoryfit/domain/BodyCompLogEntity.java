package com.trajectoryfit.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** An optional body-composition reading, stored in metric (lean mass in kg). */
@Entity
@Table(name = "body_comp_logs")
public class BodyCompLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "logged_on", nullable = false)
    private LocalDate loggedOn;

    @Column(name = "body_fat_pct")
    private Double bodyFatPct;

    @Column(name = "lean_mass_kg")
    private Double leanMassKg;

    @Column
    private String source;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getLoggedOn() { return loggedOn; }
    public void setLoggedOn(LocalDate loggedOn) { this.loggedOn = loggedOn; }

    public Double getBodyFatPct() { return bodyFatPct; }
    public void setBodyFatPct(Double bodyFatPct) { this.bodyFatPct = bodyFatPct; }

    public Double getLeanMassKg() { return leanMassKg; }
    public void setLeanMassKg(Double leanMassKg) { this.leanMassKg = leanMassKg; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public Instant getCreatedAt() { return createdAt; }
}

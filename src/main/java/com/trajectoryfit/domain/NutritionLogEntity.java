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

/**
 * One protein entry. The tier discriminator says how to read the row: {@code precise}
 * rows carry {@code grams} (meal is a specific meal); {@code quick} rows carry a
 * {@code level} on the {@code whole_day} meal; {@code estimate} rows carry a
 * {@code level} on a specific meal.
 */
@Entity
@Table(name = "nutrition_logs")
public class NutritionLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "logged_on", nullable = false)
    private LocalDate loggedOn;

    @Column(nullable = false)
    private String tier;

    @Column(nullable = false)
    private String meal;

    @Column
    private String level;

    @Column
    private Double grams;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getLoggedOn() { return loggedOn; }
    public void setLoggedOn(LocalDate loggedOn) { this.loggedOn = loggedOn; }

    public String getTier() { return tier; }
    public void setTier(String tier) { this.tier = tier; }

    public String getMeal() { return meal; }
    public void setMeal(String meal) { this.meal = meal; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public Double getGrams() { return grams; }
    public void setGrams(Double grams) { this.grams = grams; }

    public Instant getCreatedAt() { return createdAt; }
}

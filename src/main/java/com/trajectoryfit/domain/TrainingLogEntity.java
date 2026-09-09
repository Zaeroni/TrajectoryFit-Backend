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

/** A training session (or explicit rest day) for a user on a given day. */
@Entity
@Table(name = "training_logs")
public class TrainingLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "logged_on", nullable = false)
    private LocalDate loggedOn;

    /** resistance | cardio | rest */
    @Column(nullable = false)
    private String type;

    @Column(name = "duration_min")
    private Integer durationMin;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "text[]")
    private String[] muscles;

    @Column
    private String effort;

    @Column(name = "cardio_type")
    private String cardioType;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    public Long getId() { return id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public LocalDate getLoggedOn() { return loggedOn; }
    public void setLoggedOn(LocalDate loggedOn) { this.loggedOn = loggedOn; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Integer getDurationMin() { return durationMin; }
    public void setDurationMin(Integer durationMin) { this.durationMin = durationMin; }

    public String[] getMuscles() { return muscles; }
    public void setMuscles(String[] muscles) { this.muscles = muscles; }

    public String getEffort() { return effort; }
    public void setEffort(String effort) { this.effort = effort; }

    public String getCardioType() { return cardioType; }
    public void setCardioType(String cardioType) { this.cardioType = cardioType; }

    public Instant getCreatedAt() { return createdAt; }
}

package com.trajectoryfit.repo;

import com.trajectoryfit.domain.RiskScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RiskScoreRepository extends JpaRepository<RiskScoreEntity, Long> {

    Optional<RiskScoreEntity> findByUserIdAndComputedFor(UUID userId, LocalDate computedFor);

    List<RiskScoreEntity> findByUserIdOrderByComputedForAsc(UUID userId);
}

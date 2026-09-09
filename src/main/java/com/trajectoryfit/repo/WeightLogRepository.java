package com.trajectoryfit.repo;

import com.trajectoryfit.domain.WeightLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeightLogRepository extends JpaRepository<WeightLogEntity, Long> {

    List<WeightLogEntity> findByUserIdAndLoggedOnBetweenOrderByLoggedOnAsc(UUID userId, LocalDate from, LocalDate to);

    List<WeightLogEntity> findByUserIdOrderByLoggedOnAsc(UUID userId);

    Optional<WeightLogEntity> findByUserIdAndLoggedOn(UUID userId, LocalDate loggedOn);
}

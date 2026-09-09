package com.trajectoryfit.repo;

import com.trajectoryfit.domain.TrainingLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TrainingLogRepository extends JpaRepository<TrainingLogEntity, Long> {

    List<TrainingLogEntity> findByUserIdAndLoggedOnBetweenOrderByLoggedOnAsc(UUID userId, LocalDate from, LocalDate to);

    List<TrainingLogEntity> findByUserIdOrderByLoggedOnAsc(UUID userId);

    Optional<TrainingLogEntity> findByUserIdAndLoggedOn(UUID userId, LocalDate loggedOn);
}

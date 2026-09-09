package com.trajectoryfit.repo;

import com.trajectoryfit.domain.NutritionLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface NutritionLogRepository extends JpaRepository<NutritionLogEntity, Long> {

    List<NutritionLogEntity> findByUserIdAndLoggedOnBetweenOrderByLoggedOnAsc(UUID userId, LocalDate from, LocalDate to);

    List<NutritionLogEntity> findByUserIdOrderByLoggedOnAsc(UUID userId);

    List<NutritionLogEntity> findByUserIdAndLoggedOn(UUID userId, LocalDate loggedOn);

    @Transactional
    void deleteByUserIdAndLoggedOn(UUID userId, LocalDate loggedOn);
}

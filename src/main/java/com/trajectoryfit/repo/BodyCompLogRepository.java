package com.trajectoryfit.repo;

import com.trajectoryfit.domain.BodyCompLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BodyCompLogRepository extends JpaRepository<BodyCompLogEntity, Long> {

    /** All readings, oldest first — body comp looks across the whole history, not just this week. */
    List<BodyCompLogEntity> findByUserIdOrderByLoggedOnAsc(UUID userId);

    Optional<BodyCompLogEntity> findByUserIdAndLoggedOn(UUID userId, LocalDate loggedOn);
}

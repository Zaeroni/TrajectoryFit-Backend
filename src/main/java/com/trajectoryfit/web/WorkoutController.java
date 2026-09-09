package com.trajectoryfit.web;

import com.trajectoryfit.domain.TrainingLogEntity;
import com.trajectoryfit.repo.TrainingLogRepository;
import com.trajectoryfit.repo.UserRepository;
import com.trajectoryfit.web.dto.WorkoutRequest;
import com.trajectoryfit.web.dto.WorkoutResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class WorkoutController {

    private final TrainingLogRepository training;
    private final UserRepository users;

    public WorkoutController(TrainingLogRepository training, UserRepository users) {
        this.training = training;
        this.users = users;
    }

    /** Create or update the training entry for the given user and day. */
    @PostMapping("/api/workouts")
    public WorkoutResponse create(@Valid @RequestBody WorkoutRequest req) {
        requireUser(req.userId());
        TrainingLogEntity entity = training.findByUserIdAndLoggedOn(req.userId(), req.loggedOn())
                .orElseGet(TrainingLogEntity::new);
        entity.setUserId(req.userId());
        entity.setLoggedOn(req.loggedOn());
        entity.setType(req.type());
        entity.setDurationMin(req.durationMin());
        entity.setMuscles(req.muscles() == null ? null : req.muscles().toArray(new String[0]));
        entity.setEffort(req.effort());
        entity.setCardioType(req.cardioType());
        return WorkoutResponse.from(training.save(entity));
    }

    @GetMapping("/api/users/{id}/workouts")
    public List<WorkoutResponse> list(@PathVariable UUID id) {
        requireUser(id);
        return training.findByUserIdOrderByLoggedOnAsc(id).stream().map(WorkoutResponse::from).toList();
    }

    private void requireUser(UUID id) {
        if (!users.existsById(id)) {
            throw new NotFoundException("User " + id + " not found");
        }
    }
}

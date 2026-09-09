package com.trajectoryfit.web;

import com.trajectoryfit.domain.WeightLogEntity;
import com.trajectoryfit.repo.UserRepository;
import com.trajectoryfit.repo.WeightLogRepository;
import com.trajectoryfit.service.Units;
import com.trajectoryfit.web.dto.WeighInRequest;
import com.trajectoryfit.web.dto.WeighInResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class WeighInController {

    private final WeightLogRepository weights;
    private final UserRepository users;

    public WeighInController(WeightLogRepository weights, UserRepository users) {
        this.weights = weights;
        this.users = users;
    }

    /** Create or update the weight reading for the given user and day. */
    @PostMapping("/api/weigh-ins")
    public WeighInResponse create(@Valid @RequestBody WeighInRequest req) {
        requireUser(req.userId());
        WeightLogEntity entity = weights.findByUserIdAndLoggedOn(req.userId(), req.loggedOn())
                .orElseGet(WeightLogEntity::new);
        entity.setUserId(req.userId());
        entity.setLoggedOn(req.loggedOn());
        entity.setWeightKg(Units.lbToKg(req.weightLb()));
        return WeighInResponse.from(weights.save(entity));
    }

    @GetMapping("/api/users/{id}/weigh-ins")
    public List<WeighInResponse> list(@PathVariable UUID id) {
        requireUser(id);
        return weights.findByUserIdOrderByLoggedOnAsc(id).stream().map(WeighInResponse::from).toList();
    }

    private void requireUser(UUID id) {
        if (!users.existsById(id)) {
            throw new NotFoundException("User " + id + " not found");
        }
    }
}

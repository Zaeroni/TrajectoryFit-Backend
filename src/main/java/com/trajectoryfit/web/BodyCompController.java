package com.trajectoryfit.web;

import com.trajectoryfit.domain.BodyCompLogEntity;
import com.trajectoryfit.repo.BodyCompLogRepository;
import com.trajectoryfit.repo.UserRepository;
import com.trajectoryfit.service.Units;
import com.trajectoryfit.web.dto.BodyCompRequest;
import com.trajectoryfit.web.dto.BodyCompResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class BodyCompController {

    private final BodyCompLogRepository bodyComp;
    private final UserRepository users;

    public BodyCompController(BodyCompLogRepository bodyComp, UserRepository users) {
        this.bodyComp = bodyComp;
        this.users = users;
    }

    @PostMapping("/api/body-comp")
    public BodyCompResponse create(@Valid @RequestBody BodyCompRequest req) {
        requireUser(req.userId());
        if (req.bodyFatPct() == null && req.leanMassLb() == null) {
            throw new BadRequestException("Provide at least one of bodyFatPct or leanMassLb");
        }
        BodyCompLogEntity entity = bodyComp.findByUserIdAndLoggedOn(req.userId(), req.loggedOn())
                .orElseGet(BodyCompLogEntity::new);
        entity.setUserId(req.userId());
        entity.setLoggedOn(req.loggedOn());
        entity.setBodyFatPct(req.bodyFatPct());
        entity.setLeanMassKg(req.leanMassLb() == null ? null : Units.lbToKg(req.leanMassLb()));
        entity.setSource(req.source());
        return BodyCompResponse.from(bodyComp.save(entity));
    }

    @GetMapping("/api/users/{id}/body-comp")
    public List<BodyCompResponse> list(@PathVariable UUID id) {
        requireUser(id);
        return bodyComp.findByUserIdOrderByLoggedOnAsc(id).stream().map(BodyCompResponse::from).toList();
    }

    private void requireUser(UUID id) {
        if (!users.existsById(id)) {
            throw new NotFoundException("User " + id + " not found");
        }
    }
}

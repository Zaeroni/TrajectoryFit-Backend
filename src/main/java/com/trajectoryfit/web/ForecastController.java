package com.trajectoryfit.web;

import com.trajectoryfit.repo.UserRepository;
import com.trajectoryfit.service.ForecastService;
import com.trajectoryfit.service.ForecastService.ForecastResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/** Phase-2 forecast endpoint. Currently a stub that returns the stable response contract. */
@RestController
public class ForecastController {

    private final ForecastService forecastService;
    private final UserRepository users;

    public ForecastController(ForecastService forecastService, UserRepository users) {
        this.forecastService = forecastService;
        this.users = users;
    }

    @GetMapping("/api/users/{id}/forecast")
    public ForecastResult forecast(@PathVariable UUID id,
                                   @RequestParam(defaultValue = "30") int horizonDays) {
        if (!users.existsById(id)) {
            throw new NotFoundException("User " + id + " not found");
        }
        if (horizonDays < 1 || horizonDays > 365) {
            throw new BadRequestException("horizonDays must be between 1 and 365");
        }
        return forecastService.forecast(id, horizonDays);
    }
}

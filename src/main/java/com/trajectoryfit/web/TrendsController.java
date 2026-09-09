package com.trajectoryfit.web;

import com.trajectoryfit.repo.UserRepository;
import com.trajectoryfit.service.TrendsService;
import com.trajectoryfit.service.TrendsService.TrendsResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

/** Returns the time series the dashboard charts: weight (+ smoothed), protein, training. */
@RestController
public class TrendsController {

    private final TrendsService trendsService;
    private final UserRepository users;

    public TrendsController(TrendsService trendsService, UserRepository users) {
        this.trendsService = trendsService;
        this.users = users;
    }

    @GetMapping("/api/users/{id}/trends")
    public TrendsResult trends(@PathVariable UUID id,
                               @RequestParam(defaultValue = "30") int range,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        if (!users.existsById(id)) {
            throw new NotFoundException("User " + id + " not found");
        }
        if (range < 1 || range > 365) {
            throw new BadRequestException("range must be between 1 and 365 days");
        }
        LocalDate anchor = date != null ? date : LocalDate.now();
        return trendsService.compute(id, anchor, range);
    }
}

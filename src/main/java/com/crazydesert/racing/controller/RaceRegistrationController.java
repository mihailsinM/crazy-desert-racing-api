package com.crazydesert.racing.controller;

import com.crazydesert.racing.RaceRegistration;
import com.crazydesert.racing.dto.RaceRegistrationCreateRequest;
import com.crazydesert.racing.service.RaceRegistrationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registrations")
public class RaceRegistrationController {

    private final RaceRegistrationService raceRegistrationService;

    public RaceRegistrationController(
            RaceRegistrationService raceRegistrationService) {
        this.raceRegistrationService = raceRegistrationService;
    }

    @PostMapping
    public RaceRegistration createRegistration(
            @Valid
            @RequestBody RaceRegistrationCreateRequest request) {

        return raceRegistrationService.createRegistration(request);
    }
}
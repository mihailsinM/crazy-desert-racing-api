package com.crazydesert.racing.controller;

import com.crazydesert.racing.RaceRegistration;
import com.crazydesert.racing.dto.RaceRegistrationCreateRequest;
import com.crazydesert.racing.service.RaceRegistrationService;
import jakarta.validation.Valid;
import com.crazydesert.racing.dto.RaceRegistrationMyCreateRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.crazydesert.racing.dto.RaceParticipantResponse;
import java.util.List;

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

    @PostMapping("/my")
    public RaceRegistration createMyRegistration(
            Authentication authentication,
            @Valid @RequestBody RaceRegistrationMyCreateRequest request) {

        String email = authentication.getName();

        return raceRegistrationService.createMyRegistration(email, request);
    }

    @GetMapping("/race/{raceId}/participants")
    public List<RaceParticipantResponse> getParticipants(
            @PathVariable Long raceId) {

        return raceRegistrationService.getParticipantsByRaceId(raceId);
    }
}
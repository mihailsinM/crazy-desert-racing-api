package com.crazydesert.racing.controller;

import com.crazydesert.racing.dto.DriverProfileResponse;
import com.crazydesert.racing.dto.DriverSummaryResponse;
import com.crazydesert.racing.dto.PublicProfileUpdateRequest;
import com.crazydesert.racing.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverService driverService;

    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @GetMapping
    public List<DriverSummaryResponse> getDrivers(
            Authentication authentication) {

        return driverService.getDrivers(authentication.getName());
    }

    @GetMapping("/{id}")
    public DriverProfileResponse getDriver(
            @PathVariable Long id,
            Authentication authentication) {

        return driverService.getDriver(id, authentication.getName());
    }

    @GetMapping("/me")
    public DriverProfileResponse getCurrentDriver(
            Authentication authentication) {

        return driverService.getCurrentDriver(authentication.getName());
    }

    @PutMapping("/me")
    public DriverProfileResponse updateCurrentProfile(
            Authentication authentication,
            @Valid @RequestBody PublicProfileUpdateRequest request) {

        return driverService.updateCurrentProfile(
                authentication.getName(),
                request
        );
    }
}

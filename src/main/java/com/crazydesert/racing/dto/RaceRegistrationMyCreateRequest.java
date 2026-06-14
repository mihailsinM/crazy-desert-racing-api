package com.crazydesert.racing.dto;

import jakarta.validation.constraints.NotNull;

public class RaceRegistrationMyCreateRequest {

    @NotNull(message = "Race car id is required")
    public Long raceCarId;

    @NotNull(message = "Race id is required")
    public Long raceId;
}
package com.crazydesert.racing.dto;

import jakarta.validation.constraints.NotNull;

public class RaceRegistrationCreateRequest {

    @NotNull(message = "User id is required")
    public Long userId;

    @NotNull(message = "Race car id is required")
    public Long raceCarId;

    @NotNull(message = "Race id is required")
    public Long raceId;
}

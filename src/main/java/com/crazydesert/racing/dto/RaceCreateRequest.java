package com.crazydesert.racing.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class RaceCreateRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 120, message = "Name must be 120 characters or fewer")
    public String name;

    @NotBlank(message = "Location must not be blank")
    @Size(max = 200, message = "Location must be 200 characters or fewer")
    public String location;

    @NotNull(message = "Start date is required")
    public LocalDate startDate;

    @Min(value = 1, message = "Max participants must be at least 1")
    @Max(value = 500, message = "Max participants must not be greater than 500")
    public int maxParticipants;

}

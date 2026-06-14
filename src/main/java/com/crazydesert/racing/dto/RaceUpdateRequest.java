package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.RaceStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class RaceUpdateRequest {

    @NotBlank(message = "Name must not be blank")
    public String name;

    @NotBlank(message = "Location must not be blank")
    public String location;

    public LocalDate startDate;

    @Min(value = 1, message = "Participants must be at least 1")
    @Max(value = 500, message = "Max participants must not be greater than 500")
    public int maxParticipants;

    public RaceStatus status;

    public String adminMessage;
}

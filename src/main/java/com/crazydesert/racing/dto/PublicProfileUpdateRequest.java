package com.crazydesert.racing.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PublicProfileUpdateRequest {

    @Size(max = 500, message = "Bio must be at most 500 characters")
    public String bio;

    @Size(max = 120, message = "Location must be at most 120 characters")
    public String location;

    @NotNull(message = "Cars visibility is required")
    public Boolean showCars;

    @NotNull(message = "Race history visibility is required")
    public Boolean showRaceHistory;

    @NotNull(message = "Photos visibility is required")
    public Boolean showPhotos;
}

package com.crazydesert.racing.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class RaceCarCreateRequest {

    @NotBlank(message = "Name must not be blank")
    public String name;

    @NotBlank(message = "Brand must not be blank")
    public String brand;

    @Min(value = 1, message = "Horse power must be at least 1")
    public int horsePower;

    @NotBlank(message = "Image URL must not be blank")
    public String imageUrl;

    public String imagePosition;
}

package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.DesertLiveCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class DesertLiveUpdateRequest {

    @NotNull(message = "Category is required")
    public DesertLiveCategory category;

    @NotBlank(message = "Title is required")
    @Size(max = 120, message = "Title must be 120 characters or fewer")
    public String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be 1000 characters or fewer")
    public String description;

    @Size(max = 500, message = "Target URL must be 500 characters or fewer")
    public String targetUrl;

    public Instant activeFrom;
    public Instant activeUntil;
}

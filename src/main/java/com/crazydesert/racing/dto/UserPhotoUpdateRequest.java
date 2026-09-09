package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.UserPhotoVisibility;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserPhotoUpdateRequest {

    @Size(max = 300, message = "Photo caption must be at most 300 characters")
    public String caption;

    @NotNull(message = "Photo visibility is required")
    public UserPhotoVisibility visibility;
}

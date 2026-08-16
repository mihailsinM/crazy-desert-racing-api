package com.crazydesert.racing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DesertLiveRejectRequest {

    @NotBlank(message = "Rejection reason is required")
    @Size(max = 500, message = "Rejection reason must be 500 characters or fewer")
    public String reason;
}

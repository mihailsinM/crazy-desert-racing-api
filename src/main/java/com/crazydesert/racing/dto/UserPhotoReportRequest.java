package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.UserPhotoReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UserPhotoReportRequest {

    @NotNull(message = "Report reason is required")
    public UserPhotoReportReason reason;

    @Size(max = 500, message = "Report details must be at most 500 characters")
    public String details;
}

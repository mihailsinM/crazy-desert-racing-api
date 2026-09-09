package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.UserPhotoReportStatus;
import jakarta.validation.constraints.NotNull;

public class PhotoReportReviewRequest {

    @NotNull(message = "Report status is required")
    public UserPhotoReportStatus status;
}

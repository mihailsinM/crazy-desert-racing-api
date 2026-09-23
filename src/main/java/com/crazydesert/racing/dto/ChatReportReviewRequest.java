package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.ChatReportStatus;
import jakarta.validation.constraints.NotNull;

public class ChatReportReviewRequest {

    @NotNull(message = "Report status is required")
    public ChatReportStatus status;
}

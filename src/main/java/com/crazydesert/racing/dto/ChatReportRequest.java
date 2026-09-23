package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.ChatReportReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ChatReportRequest {

    @NotNull(message = "Report reason is required")
    public ChatReportReason reason;

    @Size(max = 500, message = "Report details must be at most 500 characters")
    public String details;
}

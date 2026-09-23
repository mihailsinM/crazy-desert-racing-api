package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.ChatReportReason;
import com.crazydesert.racing.enums.ChatReportStatus;

import java.time.LocalDateTime;

public record ChatReportResponse(
        Long id,
        Long messageId,
        Long conversationId,
        Long reporterId,
        String reporterName,
        Long reportedUserId,
        String reportedUserName,
        ChatReportReason reason,
        String details,
        String reportedMessage,
        String imageUrl,
        ChatReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt) {
}

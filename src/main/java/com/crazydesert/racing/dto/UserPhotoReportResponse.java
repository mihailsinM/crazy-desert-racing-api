package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.UserPhotoReportReason;
import com.crazydesert.racing.enums.UserPhotoReportStatus;

import java.time.LocalDateTime;

public record UserPhotoReportResponse(
        Long id,
        Long photoId,
        Long photoOwnerId,
        String photoOwnerName,
        Long reporterId,
        String reporterName,
        UserPhotoReportReason reason,
        String details,
        UserPhotoReportStatus status,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt) {
}

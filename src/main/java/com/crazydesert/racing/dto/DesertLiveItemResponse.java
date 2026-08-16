package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.DesertLiveCategory;
import com.crazydesert.racing.enums.DesertLiveModerationStatus;
import com.crazydesert.racing.enums.DesertLiveSource;

import java.time.Instant;

public record DesertLiveItemResponse(
        Long id,
        DesertLiveCategory category,
        DesertLiveSource source,
        DesertLiveModerationStatus moderationStatus,
        String title,
        String description,
        String targetUrl,
        Long authorId,
        String authorName,
        String authorAvatarUrl,
        String moderationNote,
        Instant activeFrom,
        Instant activeUntil,
        Instant createdAt,
        Instant updatedAt,
        String imageUrl
) {
}

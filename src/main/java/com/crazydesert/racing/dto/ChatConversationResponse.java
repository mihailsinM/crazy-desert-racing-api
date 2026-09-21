package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.ChatConversationType;

import java.time.LocalDateTime;

public record ChatConversationResponse(
        Long id,
        ChatConversationType type,
        String title,
        Long otherUserId,
        String avatarUrl,
        ImageFramingProfilesResponse imageFraming,
        String lastMessagePreview,
        LocalDateTime lastMessageAt,
        long unreadCount,
        boolean blockedByMe,
        boolean blockedByOther) {
}

package com.crazydesert.racing.dto;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long conversationId,
        Long senderId,
        String senderName,
        boolean mine,
        String body,
        String imageUrl,
        String imageOriginalName,
        LocalDateTime createdAt) {
}

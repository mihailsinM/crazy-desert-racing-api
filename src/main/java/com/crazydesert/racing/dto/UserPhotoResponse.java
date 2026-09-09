package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.UserPhotoVisibility;

import java.time.LocalDateTime;

public record UserPhotoResponse(
        Long id,
        String imageUrl,
        String caption,
        UserPhotoVisibility visibility,
        LocalDateTime createdAt,
        boolean profilePhoto,
        ImageFramingProfilesResponse imageFraming) {
}

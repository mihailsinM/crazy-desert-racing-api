package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.RaceStatus;

import java.time.LocalDate;

public record RaceResponse(
        Long id,
        String name,
        String location,
        LocalDate startDate,
        int maxParticipants,
        RaceStatus status,
        String adminMessage,
        String imageUrl,
        int imageFocusX,
        int imageFocusY,
        int imageCropPercent,
        ImageFramingProfilesResponse imageFraming) {
}

package com.crazydesert.racing.dto;

import com.crazydesert.racing.enums.RaceStatus;

import java.time.LocalDate;

public record DriverRaceResponse(
        Long registrationId,
        Long raceId,
        String raceName,
        String location,
        LocalDate startDate,
        RaceStatus status,
        String imageUrl,
        ImageFramingProfilesResponse imageFraming,
        Long raceCarId,
        String raceCarName) {
}

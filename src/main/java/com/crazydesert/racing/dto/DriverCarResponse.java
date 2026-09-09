package com.crazydesert.racing.dto;

public record DriverCarResponse(
        Long id,
        String name,
        String brand,
        int horsePower,
        String imageUrl,
        ImageFramingProfilesResponse imageFraming) {
}

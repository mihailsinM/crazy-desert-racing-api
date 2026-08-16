package com.crazydesert.racing.dto;

public record DesertLiveImageResponse(
        byte[] data,
        String contentType
) {
}

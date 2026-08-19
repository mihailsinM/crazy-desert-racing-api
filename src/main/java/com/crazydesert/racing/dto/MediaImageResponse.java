package com.crazydesert.racing.dto;

public record MediaImageResponse(
        byte[] data,
        String contentType
) {
}

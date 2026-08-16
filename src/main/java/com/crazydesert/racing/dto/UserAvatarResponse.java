package com.crazydesert.racing.dto;

public record UserAvatarResponse(
        byte[] data,
        String contentType
) {
}

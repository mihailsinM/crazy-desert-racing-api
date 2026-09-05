package com.crazydesert.racing.dto;

public class ImageFramingRequest {

    public ImageFramingProfileRequest avatar;
    public ImageFramingProfileRequest card;

    // Legacy single-profile fields. They remain supported until the
    // frontend has fully migrated to explicit avatar/card profiles.
    public Integer focusX;
    public Integer focusY;
    public Integer cropPercent;

    public boolean hasExplicitProfiles() {
        return avatar != null || card != null;
    }

    public boolean hasLegacyProfile() {
        return focusX != null || focusY != null || cropPercent != null;
    }

    public static ImageFramingRequest fromParameters(
            Integer focusX,
            Integer focusY,
            Integer cropPercent,
            Integer avatarFocusX,
            Integer avatarFocusY,
            Integer avatarCropPercent,
            Integer cardFocusX,
            Integer cardFocusY,
            Integer cardCropPercent) {

        ImageFramingRequest request = new ImageFramingRequest();
        request.focusX = focusX;
        request.focusY = focusY;
        request.cropPercent = cropPercent;

        if (avatarFocusX != null
                || avatarFocusY != null
                || avatarCropPercent != null) {
            request.avatar = new ImageFramingProfileRequest(
                    avatarFocusX,
                    avatarFocusY,
                    avatarCropPercent
            );
        }

        if (cardFocusX != null
                || cardFocusY != null
                || cardCropPercent != null) {
            request.card = new ImageFramingProfileRequest(
                    cardFocusX,
                    cardFocusY,
                    cardCropPercent
            );
        }

        return request;
    }
}

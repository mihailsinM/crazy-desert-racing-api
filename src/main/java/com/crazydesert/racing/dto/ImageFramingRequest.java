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
}

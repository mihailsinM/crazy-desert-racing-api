package com.crazydesert.racing.dto;

public class ImageFramingProfileRequest {

    public Integer focusX;
    public Integer focusY;
    public Integer cropPercent;

    public ImageFramingProfileRequest() {
    }

    public ImageFramingProfileRequest(
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        this.focusX = focusX;
        this.focusY = focusY;
        this.cropPercent = cropPercent;
    }
}

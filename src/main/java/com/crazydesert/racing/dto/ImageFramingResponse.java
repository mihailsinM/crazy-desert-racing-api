package com.crazydesert.racing.dto;

import com.crazydesert.racing.ImageFraming;

public record ImageFramingResponse(
        int focusX,
        int focusY,
        int cropPercent) {

    public static ImageFramingResponse from(ImageFraming imageFraming) {
        return new ImageFramingResponse(
                imageFraming.getFocusX(),
                imageFraming.getFocusY(),
                imageFraming.getCropPercent()
        );
    }
}

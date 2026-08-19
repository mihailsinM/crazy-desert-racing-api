package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.InvalidImageFramingException;
import org.springframework.stereotype.Component;

@Component
public class ImageFramingValidator {

    public static final int MIN_CROP_PERCENT = 0;
    public static final int MAX_CROP_PERCENT = 35;
    public static final int CROP_PERCENT_STEP = 5;

    private final ImageFocusValidator imageFocusValidator;

    public ImageFramingValidator(ImageFocusValidator imageFocusValidator) {
        this.imageFocusValidator = imageFocusValidator;
    }

    public void validate(
            Integer focusX,
            Integer focusY,
            Integer cropPercent) {

        imageFocusValidator.validate(focusX, focusY);

        if (cropPercent == null) {
            throw new InvalidImageFramingException(
                    "Image crop percent is required"
            );
        }

        if (cropPercent < MIN_CROP_PERCENT
                || cropPercent > MAX_CROP_PERCENT
                || cropPercent % CROP_PERCENT_STEP != 0) {
            throw new InvalidImageFramingException(
                    "Image crop percent must be between 0 and 35 in steps of 5"
            );
        }
    }
}

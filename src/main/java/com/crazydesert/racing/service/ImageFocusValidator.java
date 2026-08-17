package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.InvalidImageFocusException;
import org.springframework.stereotype.Component;

@Component
public class ImageFocusValidator {

    public static final int DEFAULT_FOCUS = 50;
    public static final int MIN_FOCUS = 0;
    public static final int MAX_FOCUS = 100;

    public void validate(Integer focusX, Integer focusY) {
        if (focusX == null || focusY == null) {
            throw new InvalidImageFocusException(
                    "Both image focus coordinates are required"
            );
        }

        if (!isInRange(focusX) || !isInRange(focusY)) {
            throw new InvalidImageFocusException(
                    "Image focus coordinates must be between 0 and 100"
            );
        }
    }

    private boolean isInRange(int value) {
        return value >= MIN_FOCUS && value <= MAX_FOCUS;
    }
}

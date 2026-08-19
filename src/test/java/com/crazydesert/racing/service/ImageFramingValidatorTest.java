package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.InvalidImageFramingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageFramingValidatorTest {

    private final ImageFramingValidator validator =
            new ImageFramingValidator(new ImageFocusValidator());

    @Test
    void acceptsSupportedCropSteps() {
        assertDoesNotThrow(() -> validator.validate(0, 100, 0));
        assertDoesNotThrow(() -> validator.validate(50, 50, 5));
        assertDoesNotThrow(() -> validator.validate(50, 50, 10));
        assertDoesNotThrow(() -> validator.validate(50, 50, 15));
        assertDoesNotThrow(() -> validator.validate(50, 50, 20));
        assertDoesNotThrow(() -> validator.validate(50, 50, 25));
        assertDoesNotThrow(() -> validator.validate(50, 50, 30));
        assertDoesNotThrow(() -> validator.validate(100, 0, 35));
    }

    @Test
    void rejectsMissingCropPercent() {
        assertThrows(
                InvalidImageFramingException.class,
                () -> validator.validate(50, 50, null)
        );
    }

    @Test
    void rejectsCropOutsideSupportedSteps() {
        assertThrows(
                InvalidImageFramingException.class,
                () -> validator.validate(50, 50, -5)
        );
        assertThrows(
                InvalidImageFramingException.class,
                () -> validator.validate(50, 50, 12)
        );
        assertThrows(
                InvalidImageFramingException.class,
                () -> validator.validate(50, 50, 40)
        );
    }
}

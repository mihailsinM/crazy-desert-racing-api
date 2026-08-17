package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.InvalidImageFocusException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageFocusValidatorTest {

    private final ImageFocusValidator validator =
            new ImageFocusValidator();

    @Test
    void acceptsBoundaryCoordinates() {
        assertDoesNotThrow(() -> validator.validate(0, 100));
        assertDoesNotThrow(() -> validator.validate(100, 0));
    }

    @Test
    void rejectsMissingCoordinate() {
        assertThrows(
                InvalidImageFocusException.class,
                () -> validator.validate(null, 50)
        );
    }

    @Test
    void rejectsCoordinateOutsidePercentageRange() {
        assertThrows(
                InvalidImageFocusException.class,
                () -> validator.validate(-1, 50)
        );
        assertThrows(
                InvalidImageFocusException.class,
                () -> validator.validate(50, 101)
        );
    }
}

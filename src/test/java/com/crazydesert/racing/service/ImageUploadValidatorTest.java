package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.InvalidImageException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ImageUploadValidatorTest {

    private final ImageUploadValidator imageUploadValidator =
            new ImageUploadValidator();

    @Test
    void acceptsValidPngImage() {
        byte[] imageData = new byte[]{
                (byte) 0x89,
                0x50,
                0x4e,
                0x47,
                0x0d,
                0x0a,
                0x1a,
                0x0a
        };
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "desert.png",
                "image/png",
                imageData
        );

        assertArrayEquals(
                imageData,
                imageUploadValidator.validateAndRead(image)
        );
    }

    @Test
    void rejectsUnsupportedImageType() {
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "desert.svg",
                "image/svg+xml",
                new byte[]{1, 2, 3}
        );

        assertThrows(
                InvalidImageException.class,
                () -> imageUploadValidator.validateAndRead(image)
        );
    }

    @Test
    void rejectsImageWithInvalidSignature() {
        MockMultipartFile image = new MockMultipartFile(
                "file",
                "fake.png",
                "image/png",
                "not-an-image".getBytes()
        );

        assertThrows(
                InvalidImageException.class,
                () -> imageUploadValidator.validateAndRead(image)
        );
    }
}

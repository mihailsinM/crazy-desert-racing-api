package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.ImageStorageException;
import com.crazydesert.racing.exception.InvalidImageException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@Component
public class ImageUploadValidator {

    private static final long MAX_IMAGE_SIZE_BYTES = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public byte[] validateAndRead(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new InvalidImageException("Image must not be empty");
        }

        if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new InvalidImageException("Image must be 2 MB or smaller");
        }

        String contentType = image.getContentType();

        if (!ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidImageException(
                    "Image must be a JPG, PNG, or WebP file"
            );
        }

        byte[] imageData;

        try {
            imageData = image.getBytes();
        } catch (IOException exception) {
            throw new ImageStorageException(
                    "Failed to read image",
                    exception
            );
        }

        if (!hasExpectedImageSignature(imageData, contentType)) {
            throw new InvalidImageException(
                    "Image content does not match its file type"
            );
        }

        return imageData;
    }

    private boolean hasExpectedImageSignature(
            byte[] data,
            String contentType) {

        return switch (contentType) {
            case "image/jpeg" -> isJpeg(data);
            case "image/png" -> isPng(data);
            case "image/webp" -> isWebp(data);
            default -> false;
        };
    }

    private boolean isJpeg(byte[] data) {
        return data.length >= 3
                && (data[0] & 0xff) == 0xff
                && (data[1] & 0xff) == 0xd8
                && (data[2] & 0xff) == 0xff;
    }

    private boolean isPng(byte[] data) {
        return data.length >= 8
                && (data[0] & 0xff) == 0x89
                && data[1] == 0x50
                && data[2] == 0x4e
                && data[3] == 0x47
                && data[4] == 0x0d
                && data[5] == 0x0a
                && data[6] == 0x1a
                && data[7] == 0x0a;
    }

    private boolean isWebp(byte[] data) {
        return data.length >= 12
                && data[0] == 'R'
                && data[1] == 'I'
                && data[2] == 'F'
                && data[3] == 'F'
                && data[8] == 'W'
                && data[9] == 'E'
                && data[10] == 'B'
                && data[11] == 'P';
    }
}

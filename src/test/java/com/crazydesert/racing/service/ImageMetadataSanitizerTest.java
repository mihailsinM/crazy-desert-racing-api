package com.crazydesert.racing.service;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageMetadataSanitizerTest {

    private final ImageMetadataSanitizer sanitizer =
            new ImageMetadataSanitizer();

    @Test
    void removesExifSegmentFromJpeg() {
        byte[] jpeg = new byte[]{
                (byte) 0xff, (byte) 0xd8,
                (byte) 0xff, (byte) 0xe1, 0x00, 0x06,
                'E', 'x', 'i', 'f',
                (byte) 0xff, (byte) 0xda,
                0x01, 0x02, (byte) 0xff, (byte) 0xd9
        };

        byte[] sanitized = sanitizer.sanitize(jpeg, "image/jpeg");

        assertArrayEquals(new byte[]{
                (byte) 0xff, (byte) 0xd8,
                (byte) 0xff, (byte) 0xda,
                0x01, 0x02, (byte) 0xff, (byte) 0xd9
        }, sanitized);
    }

    @Test
    void removesLocationMetadataChunkFromPng() {
        ByteArrayOutputStream png = new ByteArrayOutputStream();
        png.writeBytes(new byte[]{
                (byte) 0x89, 0x50, 0x4e, 0x47,
                0x0d, 0x0a, 0x1a, 0x0a
        });
        writePngChunk(png, "eXIf", new byte[]{1, 2, 3, 4});
        writePngChunk(png, "IEND", new byte[0]);

        byte[] sanitized = sanitizer.sanitize(
                png.toByteArray(),
                "image/png"
        );

        String binaryText = new String(
                sanitized,
                StandardCharsets.ISO_8859_1
        );
        assertFalse(binaryText.contains("eXIf"));
        assertTrue(binaryText.contains("IEND"));
    }

    @Test
    void leavesUnknownContentTypeUnchanged() {
        byte[] data = new byte[]{1, 2, 3};

        assertArrayEquals(
                data,
                sanitizer.sanitize(data, "application/octet-stream")
        );
    }

    private void writePngChunk(
            ByteArrayOutputStream output,
            String type,
            byte[] data) {

        int length = data.length;
        output.write((length >>> 24) & 0xff);
        output.write((length >>> 16) & 0xff);
        output.write((length >>> 8) & 0xff);
        output.write(length & 0xff);
        output.writeBytes(type.getBytes(StandardCharsets.US_ASCII));
        output.writeBytes(data);
        output.writeBytes(new byte[4]);
    }
}

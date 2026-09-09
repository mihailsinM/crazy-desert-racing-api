package com.crazydesert.racing.service;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

@Component
public class ImageMetadataSanitizer {

    private static final Set<String> REMOVED_PNG_CHUNKS = Set.of(
            "eXIf",
            "tEXt",
            "zTXt",
            "iTXt",
            "tIME"
    );

    public byte[] sanitize(byte[] data, String contentType) {
        if (data == null) {
            return null;
        }

        return switch (contentType) {
            case "image/jpeg" -> stripJpegMetadata(data);
            case "image/png" -> stripPngMetadata(data);
            case "image/webp" -> stripWebpMetadata(data);
            default -> Arrays.copyOf(data, data.length);
        };
    }

    private byte[] stripJpegMetadata(byte[] data) {
        if (data.length < 4
                || unsigned(data[0]) != 0xff
                || unsigned(data[1]) != 0xd8) {
            return Arrays.copyOf(data, data.length);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
        output.write(data[0]);
        output.write(data[1]);
        int offset = 2;

        while (offset < data.length) {
            if (unsigned(data[offset]) != 0xff) {
                output.write(data, offset, data.length - offset);
                break;
            }

            int markerStart = offset;
            while (offset < data.length && unsigned(data[offset]) == 0xff) {
                offset++;
            }

            if (offset >= data.length) {
                return Arrays.copyOf(data, data.length);
            }

            int marker = unsigned(data[offset++]);

            if (marker == 0xda || marker == 0xd9) {
                output.write(data, markerStart, data.length - markerStart);
                break;
            }

            if (isStandaloneJpegMarker(marker)) {
                output.write(data, markerStart, offset - markerStart);
                continue;
            }

            if (offset + 2 > data.length) {
                return Arrays.copyOf(data, data.length);
            }

            int segmentLength = readUnsignedShortBigEndian(data, offset);
            if (segmentLength < 2 || offset + segmentLength > data.length) {
                return Arrays.copyOf(data, data.length);
            }

            boolean metadataSegment = marker == 0xe1
                    || marker == 0xed
                    || marker == 0xfe;

            if (!metadataSegment) {
                output.write(
                        data,
                        markerStart,
                        offset + segmentLength - markerStart
                );
            }

            offset += segmentLength;
        }

        return output.toByteArray();
    }

    private byte[] stripPngMetadata(byte[] data) {
        if (data.length <= 8) {
            return Arrays.copyOf(data, data.length);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
        output.write(data, 0, 8);
        int offset = 8;

        while (offset + 12 <= data.length) {
            long chunkLength = readUnsignedIntBigEndian(data, offset);
            long chunkEnd = offset + 12L + chunkLength;

            if (chunkLength > Integer.MAX_VALUE || chunkEnd > data.length) {
                return Arrays.copyOf(data, data.length);
            }

            String chunkType = new String(
                    data,
                    offset + 4,
                    4,
                    StandardCharsets.US_ASCII
            );

            if (!REMOVED_PNG_CHUNKS.contains(chunkType)) {
                output.write(data, offset, (int) (12L + chunkLength));
            }

            offset = (int) chunkEnd;

            if ("IEND".equals(chunkType)) {
                break;
            }
        }

        if (offset < data.length) {
            output.write(data, offset, data.length - offset);
        }

        return output.toByteArray();
    }

    private byte[] stripWebpMetadata(byte[] data) {
        if (data.length < 12
                || !hasAscii(data, 0, "RIFF")
                || !hasAscii(data, 8, "WEBP")) {
            return Arrays.copyOf(data, data.length);
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream(data.length);
        output.write(data, 0, 12);
        int offset = 12;

        while (offset + 8 <= data.length) {
            String chunkType = new String(
                    data,
                    offset,
                    4,
                    StandardCharsets.US_ASCII
            );
            long chunkLength = readUnsignedIntLittleEndian(data, offset + 4);
            long paddedLength = chunkLength + (chunkLength % 2);
            long chunkEnd = offset + 8L + paddedLength;

            if (chunkLength > Integer.MAX_VALUE || chunkEnd > data.length) {
                return Arrays.copyOf(data, data.length);
            }

            if (!"EXIF".equals(chunkType) && !"XMP ".equals(chunkType)) {
                byte[] chunk = Arrays.copyOfRange(data, offset, (int) chunkEnd);

                if ("VP8X".equals(chunkType) && chunkLength >= 1) {
                    chunk[8] = (byte) (chunk[8] & ~0x0c);
                }

                output.writeBytes(chunk);
            }

            offset = (int) chunkEnd;
        }

        byte[] sanitized = output.toByteArray();
        writeUnsignedIntLittleEndian(sanitized, 4, sanitized.length - 8L);
        return sanitized;
    }

    private boolean isStandaloneJpegMarker(int marker) {
        return marker == 0x01
                || (marker >= 0xd0 && marker <= 0xd7);
    }

    private int readUnsignedShortBigEndian(byte[] data, int offset) {
        return (unsigned(data[offset]) << 8)
                | unsigned(data[offset + 1]);
    }

    private long readUnsignedIntBigEndian(byte[] data, int offset) {
        return ((long) unsigned(data[offset]) << 24)
                | ((long) unsigned(data[offset + 1]) << 16)
                | ((long) unsigned(data[offset + 2]) << 8)
                | unsigned(data[offset + 3]);
    }

    private long readUnsignedIntLittleEndian(byte[] data, int offset) {
        return unsigned(data[offset])
                | ((long) unsigned(data[offset + 1]) << 8)
                | ((long) unsigned(data[offset + 2]) << 16)
                | ((long) unsigned(data[offset + 3]) << 24);
    }

    private void writeUnsignedIntLittleEndian(
            byte[] data,
            int offset,
            long value) {

        data[offset] = (byte) value;
        data[offset + 1] = (byte) (value >>> 8);
        data[offset + 2] = (byte) (value >>> 16);
        data[offset + 3] = (byte) (value >>> 24);
    }

    private boolean hasAscii(byte[] data, int offset, String value) {
        byte[] expected = value.getBytes(StandardCharsets.US_ASCII);

        if (offset + expected.length > data.length) {
            return false;
        }

        for (int index = 0; index < expected.length; index++) {
            if (data[offset + index] != expected[index]) {
                return false;
            }
        }

        return true;
    }

    private int unsigned(byte value) {
        return value & 0xff;
    }
}

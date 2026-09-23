package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.ChatEncryptionException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ChatCipherTest {

    private static final String TEST_KEY =
            "AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=";

    private final ChatCipher chatCipher = new ChatCipher(TEST_KEY);

    @Test
    void encryptsAndDecryptsTextWithoutStoringPlaintext() {
        String encrypted = chatCipher.encryptText("Meet at the desert camp");

        assertNotEquals("Meet at the desert camp", encrypted);
        assertEquals(
                "Meet at the desert camp",
                chatCipher.decryptText(encrypted)
        );
    }

    @Test
    void encryptsAndDecryptsAttachmentBytes() {
        byte[] original = "image-bytes".getBytes(StandardCharsets.UTF_8);
        byte[] encrypted = chatCipher.encryptBytes(original);

        assertArrayEquals(original, chatCipher.decryptBytes(encrypted));
    }

    @Test
    void rejectsTamperedCiphertext() {
        byte[] encrypted = chatCipher.encryptBytes(
                "private message".getBytes(StandardCharsets.UTF_8)
        );
        encrypted[encrypted.length - 1] ^= 1;

        assertThrows(
                ChatEncryptionException.class,
                () -> chatCipher.decryptBytes(encrypted)
        );
    }

    @Test
    void requiresExactlyThirtyTwoKeyBytes() {
        assertThrows(
                IllegalStateException.class,
                () -> new ChatCipher("c2hvcnQ=")
        );
    }
}

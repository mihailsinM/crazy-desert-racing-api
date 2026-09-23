package com.crazydesert.racing.service;

import com.crazydesert.racing.exception.ChatEncryptionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class ChatCipher {

    private static final byte FORMAT_VERSION = 1;
    private static final int KEY_LENGTH_BYTES = 32;
    private static final int NONCE_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;
    private final SecureRandom secureRandom = new SecureRandom();

    public ChatCipher(
            @Value("${crazy.chat.encryption-key}") String encodedKey) {

        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(encodedKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "CHAT_ENCRYPTION_KEY must be valid Base64",
                    exception
            );
        }

        if (keyBytes.length != KEY_LENGTH_BYTES) {
            throw new IllegalStateException(
                    "CHAT_ENCRYPTION_KEY must decode to exactly 32 bytes"
            );
        }

        this.key = new SecretKeySpec(keyBytes, "AES");
    }

    public String encryptText(String plaintext) {
        if (plaintext == null) {
            return null;
        }

        return Base64.getEncoder().encodeToString(
                encryptBytes(plaintext.getBytes(StandardCharsets.UTF_8))
        );
    }

    public String decryptText(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }

        try {
            return new String(
                    decryptBytes(Base64.getDecoder().decode(ciphertext)),
                    StandardCharsets.UTF_8
            );
        } catch (IllegalArgumentException exception) {
            throw new ChatEncryptionException(
                    "Stored chat text is not valid encrypted data",
                    exception
            );
        }
    }

    public byte[] encryptBytes(byte[] plaintext) {
        if (plaintext == null) {
            return null;
        }

        byte[] nonce = new byte[NONCE_LENGTH_BYTES];
        secureRandom.nextBytes(nonce);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
            );
            byte[] encrypted = cipher.doFinal(plaintext);
            byte[] payload = new byte[1 + nonce.length + encrypted.length];
            payload[0] = FORMAT_VERSION;
            System.arraycopy(nonce, 0, payload, 1, nonce.length);
            System.arraycopy(
                    encrypted,
                    0,
                    payload,
                    1 + nonce.length,
                    encrypted.length
            );
            return payload;
        } catch (GeneralSecurityException exception) {
            throw new ChatEncryptionException(
                    "Failed to encrypt chat data",
                    exception
            );
        }
    }

    public byte[] decryptBytes(byte[] payload) {
        if (payload == null) {
            return null;
        }

        if (payload.length <= 1 + NONCE_LENGTH_BYTES
                || payload[0] != FORMAT_VERSION) {
            throw new ChatEncryptionException(
                    "Stored chat data has an unsupported format",
                    null
            );
        }

        byte[] nonce = Arrays.copyOfRange(
                payload,
                1,
                1 + NONCE_LENGTH_BYTES
        );
        byte[] encrypted = Arrays.copyOfRange(
                payload,
                1 + NONCE_LENGTH_BYTES,
                payload.length
        );

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    new GCMParameterSpec(TAG_LENGTH_BITS, nonce)
            );
            return cipher.doFinal(encrypted);
        } catch (GeneralSecurityException exception) {
            throw new ChatEncryptionException(
                    "Failed to decrypt chat data",
                    exception
            );
        }
    }
}

package com.company.dataextract.util;

import com.company.dataextract.config.EncryptionProperties;
import com.company.dataextract.exception.EncryptionException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordCryptoUtil {
    private static final String PREFIX = "ENC(";
    private static final String SUFFIX = ")";
    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final String KEY_FACTORY = "PBKDF2WithHmacSHA256";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    private final EncryptionProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordCryptoUtil(EncryptionProperties properties) {
        this.properties = properties;
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return PREFIX + Base64.getEncoder().encodeToString(buffer.array()) + SUFFIX;
        } catch (GeneralSecurityException ex) {
            throw new EncryptionException("Failed to encrypt value", ex);
        }
    }

    public String decrypt(String encryptedText) {
        try {
            String normalized = unwrap(encryptedText);
            byte[] payload = Base64.getDecoder().decode(normalized);
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);
            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey(), new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new EncryptionException("Failed to decrypt value", ex);
        }
    }

    public String decryptIfEncrypted(String value) {
        if (value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX)) {
            return decrypt(value);
        }
        return value;
    }

    private SecretKey secretKey() throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY);
        KeySpec spec = new PBEKeySpec(
                properties.getSecretKey().toCharArray(),
                properties.getSalt().getBytes(StandardCharsets.UTF_8),
                properties.getIterations(),
                properties.getKeyLength());
        SecretKey secret = factory.generateSecret(spec);
        return new SecretKeySpec(secret.getEncoded(), "AES");
    }

    private String unwrap(String value) {
        if (value != null && value.startsWith(PREFIX) && value.endsWith(SUFFIX)) {
            return value.substring(PREFIX.length(), value.length() - SUFFIX.length());
        }
        return value;
    }
}

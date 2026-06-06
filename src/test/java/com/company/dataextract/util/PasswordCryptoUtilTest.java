package com.company.dataextract.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.company.dataextract.config.EncryptionProperties;
import org.junit.jupiter.api.Test;

class PasswordCryptoUtilTest {
    @Test
    void encryptsAndDecryptsValue() {
        EncryptionProperties properties = new EncryptionProperties();
        properties.setSecretKey("unit-test-secret");
        properties.setSalt("unit-test-salt");
        PasswordCryptoUtil util = new PasswordCryptoUtil(properties);

        String encrypted = util.encrypt("password");

        assertNotEquals("password", encrypted);
        assertEquals("password", util.decrypt(encrypted));
        assertEquals("plain", util.decryptIfEncrypted("plain"));
    }
}

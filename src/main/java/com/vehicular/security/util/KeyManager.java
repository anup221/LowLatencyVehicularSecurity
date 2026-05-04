package com.vehicular.security.util;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.util.Base64;

public class KeyManager {

    private static final String SHARED_KEY = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";

    public static String generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("ChaCha20");
        keyGen.init(256);
        SecretKey key = keyGen.generateKey();
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static byte[] getSharedKey() {
        return Base64.getDecoder().decode(SHARED_KEY);
    }
}
package com.vehicular.security.crypto;

import java.security.MessageDigest;
import java.util.Arrays;

public class SessionKeyManager {

    public static byte[] deriveChaChaKey(byte[] sharedSecret) throws Exception {

        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        byte[] hash = digest.digest(sharedSecret);

        return Arrays.copyOf(hash, 32); // 256-bit session key
    }
}
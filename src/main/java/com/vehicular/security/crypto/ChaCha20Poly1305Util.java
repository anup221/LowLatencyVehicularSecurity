package com.vehicular.security.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;

public class ChaCha20Poly1305Util {

    public static byte[] generateNonce() {
        byte[] nonce = new byte[12]; // Required for Poly1305
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static byte[] encrypt(byte[] key, byte[] nonce, String message) throws Exception {

        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");

        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");

        IvParameterSpec ivSpec = new IvParameterSpec(nonce);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        return cipher.doFinal(message.getBytes());
    }

    public static String decrypt(byte[] key, byte[] nonce, byte[] cipherText) throws Exception {

        Cipher cipher = Cipher.getInstance("ChaCha20-Poly1305");

        SecretKeySpec keySpec = new SecretKeySpec(key, "ChaCha20");

        IvParameterSpec ivSpec = new IvParameterSpec(nonce);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        byte[] decrypted = cipher.doFinal(cipherText);

        return new String(decrypted);
    }
}
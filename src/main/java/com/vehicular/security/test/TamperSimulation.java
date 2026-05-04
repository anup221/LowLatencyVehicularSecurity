package com.vehicular.security.test;

import com.vehicular.security.crypto.ChaCha20Poly1305Util;
import com.vehicular.security.util.KeyManager;

public class TamperSimulation {

    public static void main(String[] args) {

        try {
            String originalMessage = "Accident Ahead";

            byte[] nonce = ChaCha20Poly1305Util.generateNonce();

            byte[] encrypted = ChaCha20Poly1305Util.encrypt(
                    KeyManager.getSharedKey(),
                    nonce,
                    originalMessage
            );

            // Tamper one byte
            encrypted[0] ^= 1;

            String decrypted = ChaCha20Poly1305Util.decrypt(
                    KeyManager.getSharedKey(),
                    nonce,
                    encrypted
            );

            System.out.println("Tampered Message: " + decrypted);

        } catch (Exception e) {
            System.out.println("Authentication Failed! Packet Rejected.");
        }
    }
}
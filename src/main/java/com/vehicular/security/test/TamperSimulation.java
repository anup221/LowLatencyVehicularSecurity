package com.vehicular.security.attack;

import com.vehicular.security.crypto.*;

import java.security.KeyPair;
import java.security.PublicKey;

public class TamperSimulation {

    public static void main(String[] args) {

        try {

            KeyPair senderKeyPair = ECDHKeyExchange.generateKeyPair();
            KeyPair receiverKeyPair = ECDHKeyExchange.generateKeyPair();

            PublicKey senderPublicKey = senderKeyPair.getPublic();
            PublicKey receiverPublicKey = receiverKeyPair.getPublic();

            byte[] senderSharedSecret = ECDHKeyExchange.generateSharedSecret(
                    senderKeyPair.getPrivate(),
                    receiverPublicKey
            );

            byte[] receiverSharedSecret = ECDHKeyExchange.generateSharedSecret(
                    receiverKeyPair.getPrivate(),
                    senderPublicKey
            );

            byte[] senderSessionKey = SessionKeyManager.deriveChaChaKey(senderSharedSecret);
            byte[] receiverSessionKey = SessionKeyManager.deriveChaChaKey(receiverSharedSecret);

            if (!java.util.Arrays.equals(senderSessionKey, receiverSessionKey)) {
                System.out.println("Session Key Mismatch! Abort.");
                return;
            }

            System.out.println("ECDH Secure Session Established Successfully.");

            String originalMessage = "Accident Ahead near Junction 5";

            byte[] nonce = ChaCha20Poly1305Util.generateNonce();

            byte[] encryptedMessage = ChaCha20Poly1305Util.encrypt(
                    senderSessionKey,
                    nonce,
                    originalMessage
            );

            System.out.println("Original encrypted packet created successfully.");

            // Blind attacker tampering
            encryptedMessage[0] ^= 1;

            System.out.println("Packet tampered by attacker.");

            try {

                String decryptedMessage = ChaCha20Poly1305Util.decrypt(
                        receiverSessionKey,
                        nonce,
                        encryptedMessage
                );

                System.out.println("Tampered Message Decrypted: " + decryptedMessage);

            } catch (Exception e) {

                System.out.println("Authentication Failed! Packet Rejected.");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
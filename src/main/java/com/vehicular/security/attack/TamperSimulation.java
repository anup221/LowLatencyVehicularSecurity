package com.vehicular.security.attack;

import com.vehicular.security.crypto.*;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

public class TamperSimulation {

    public static void main(String[] args) {

        try {

            KeyPair senderKeyPair = ECDHKeyExchange.generateKeyPair();
            KeyPair receiverKeyPair = ECDHKeyExchange.generateKeyPair();

            PublicKey senderPublicKey = senderKeyPair.getPublic();
            PublicKey receiverPublicKey = receiverKeyPair.getPublic();

            System.out.println("=== KEY PAIR GENERATION ===");
            System.out.println("[Sender] Private Key: " + Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded()));
            System.out.println("[Sender] Public Key:  " + Base64.getEncoder().encodeToString(senderPublicKey.getEncoded()) + "\n");
            
            System.out.println("[Receiver] Private Key: " + Base64.getEncoder().encodeToString(receiverKeyPair.getPrivate().getEncoded()));
            System.out.println("[Receiver] Public Key:  " + Base64.getEncoder().encodeToString(receiverPublicKey.getEncoded()) + "\n");

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

            System.out.println("=== SESSION ESTABLISHED ===");
            System.out.println("[System] ECDH Secure Session Established Successfully.\n");

            String originalMessage = "Accident Ahead near Junction 5";

            byte[] nonce = ChaCha20Poly1305Util.generateNonce();

            byte[] encryptedMessage = ChaCha20Poly1305Util.encrypt(
                    senderSessionKey,
                    nonce,
                    originalMessage
            );

            System.out.println("[Sender] Original encrypted packet created successfully and sent over network.");

            // Blind attacker tampering
            System.out.println("\n=== MAN IN THE MIDDLE ATTACK ===");
            encryptedMessage[0] ^= 1;
            System.out.println("[Attacker] Intercepted packet and blindly tampered with the ciphertext bytes.");

            System.out.println("\n=== RECEIVER SIDE PROCESSING ===");
            try {

                String decryptedMessage = ChaCha20Poly1305Util.decrypt(
                        receiverSessionKey,
                        nonce,
                        encryptedMessage
                );

                System.out.println("[Receiver] Tampered Message Decrypted: " + decryptedMessage);

            } catch (Exception e) {

                System.out.println("[Receiver] Authentication Failed! Packet Rejected.");
                System.out.println("[System] Poly1305 detected the tampering. The attacker cannot spoof the MAC without the session key.");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
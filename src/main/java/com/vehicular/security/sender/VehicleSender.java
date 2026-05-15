package com.vehicular.security.sender;

import com.vehicular.security.crypto.*;
import com.vehicular.security.model.SecurePacket;
import com.vehicular.security.util.NetworkConfig;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Scanner;

public class VehicleSender {

    public static void main(String[] args) {

        try (
                Socket socket = new Socket(NetworkConfig.RECEIVER_IP, NetworkConfig.PORT);
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Scanner scanner = new Scanner(System.in)
        ) {

            KeyPair senderKeyPair = ECDHKeyExchange.generateKeyPair();

            System.out.println("=== ECDH SENDER KEYS GENERATED ===");
            System.out.println("Private Key: " + java.util.Base64.getEncoder().encodeToString(senderKeyPair.getPrivate().getEncoded()));
            System.out.println("Public Key:  " + java.util.Base64.getEncoder().encodeToString(senderKeyPair.getPublic().getEncoded()) + "\n");

            out.writeObject(senderKeyPair.getPublic().getEncoded());
            out.flush();

            byte[] receiverPublicBytes = (byte[]) in.readObject();

            PublicKey receiverPublicKey = ECDHKeyExchange.decodePublicKey(receiverPublicBytes);

            byte[] sharedSecret = ECDHKeyExchange.generateSharedSecret(
                    senderKeyPair.getPrivate(),
                    receiverPublicKey
            );

            byte[] sessionKey = SessionKeyManager.deriveChaChaKey(sharedSecret);

            System.out.println("Secure session established.");

            while (true) {
                System.out.print("\nEnter Vehicle ID (or 'exit' to quit): ");
                String vehicleId = scanner.nextLine();
                if ("exit".equalsIgnoreCase(vehicleId)) break;

                System.out.print("Enter Alert Type: ");
                String alertType = scanner.nextLine();

                System.out.print("Enter Emergency Message: ");
                String message = scanner.nextLine();

                byte[] nonce = ChaCha20Poly1305Util.generateNonce();

                byte[] encryptedMessage = ChaCha20Poly1305Util.encrypt(
                        sessionKey,
                        nonce,
                        message
                );

                SecurePacket packet = new SecurePacket(
                        vehicleId,
                        alertType,
                        encryptedMessage,
                        nonce
                );

                out.writeObject(packet);
                out.flush();

                System.out.println("Secure Packet Sent Successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
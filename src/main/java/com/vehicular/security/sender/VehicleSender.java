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

            System.out.print("Enter Vehicle ID: ");
            String vehicleId = scanner.nextLine();

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
package com.vehicular.security.receiver;

import com.vehicular.security.crypto.*;
import com.vehicular.security.model.SecurePacket;
import com.vehicular.security.util.NetworkConfig;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.PublicKey;

public class VehicleReceiver {

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(NetworkConfig.PORT)) {

            System.out.println("Vehicle Receiver Started...");
            System.out.println("Listening on port " + NetworkConfig.PORT);

            try (
                    Socket socket = serverSocket.accept();
                    ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream())
            ) {

                KeyPair receiverKeyPair = ECDHKeyExchange.generateKeyPair();

                byte[] senderPublicBytes = (byte[]) in.readObject();

                PublicKey senderPublicKey = ECDHKeyExchange.decodePublicKey(senderPublicBytes);

                out.writeObject(receiverKeyPair.getPublic().getEncoded());
                out.flush();

                byte[] sharedSecret = ECDHKeyExchange.generateSharedSecret(
                        receiverKeyPair.getPrivate(),
                        senderPublicKey
                );

                byte[] sessionKey = SessionKeyManager.deriveChaChaKey(sharedSecret);

                System.out.println("Secure session established.");

                SecurePacket packet = (SecurePacket) in.readObject();

                String decryptedMessage = ChaCha20Poly1305Util.decrypt(
                        sessionKey,
                        packet.getNonce(),
                        packet.getEncryptedMessage()
                );

                System.out.println("\n===== SECURE ALERT RECEIVED =====");
                System.out.println("Vehicle ID: " + packet.getVehicleId());
                System.out.println("Alert Type: " + packet.getAlertType());
                System.out.println("Message   : " + decryptedMessage);
                System.out.println("=================================");

            }

        } catch (Exception e) {
            System.out.println("Authentication Failed! Packet Rejected.");
        }
    }
}
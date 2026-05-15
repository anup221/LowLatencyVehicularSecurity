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

            while (true) {
                Socket socket = serverSocket.accept();
                String clientIp = socket.getInetAddress().getHostAddress();

                if (!NetworkConfig.AUTHORIZED_VEHICLE_IPS.contains(clientIp)) {
                    System.out.println("\n[SECURITY ALERT] Unauthorized connection attempt from IP: " + clientIp);
                    System.out.println("Dropping connection. DH Key Exchange aborted.");
                    socket.close();
                    continue;
                }

                System.out.println("New authorized vehicle connected: " + clientIp);
                new Thread(new ClientHandler(socket)).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
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
            System.out.println("Secure session established with " + socket.getInetAddress());

            while (true) {
                try {
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
                } catch (EOFException e) {
                    System.out.println("Vehicle disconnected.");
                    break;
                } catch (Exception e) {
                    System.out.println("Authentication Failed! Packet Rejected or Stream Corrupted.");
                    break;
                }
            }

        } catch (Exception e) {
            System.out.println("Connection error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
}
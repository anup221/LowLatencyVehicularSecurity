package com.vehicular.security.receiver;

import com.vehicular.security.crypto.ChaCha20Poly1305Util;
import com.vehicular.security.model.SecurePacket;
import com.vehicular.security.util.KeyManager;
import com.vehicular.security.util.NetworkConfig;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class VehicleReceiver {

    public static void main(String[] args) {
        System.out.println("Vehicle Receiver Started...");
        System.out.println("Listening on port " + NetworkConfig.PORT);

        try (ServerSocket serverSocket = new ServerSocket(NetworkConfig.PORT)) {

            while (true) {
                Socket socket = serverSocket.accept();

                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

                SecurePacket packet = (SecurePacket) ois.readObject();

                String message = ChaCha20Poly1305Util.decrypt(
                        KeyManager.getSharedKey(),
                        packet.getNonce(),
                        packet.getCipherText()
                );

                System.out.println("\n===== SECURE ALERT RECEIVED =====");
                System.out.println("Vehicle ID: " + packet.getVehicleId());
                System.out.println("Timestamp : " + packet.getTimestamp());
                System.out.println("Alert Type: " + packet.getAlertType());
                System.out.println("Message   : " + message);
                System.out.println("=================================\n");

                socket.close();
            }

        } catch (Exception e) {
            System.out.println("Authentication Failed or Packet Rejected!");
            e.printStackTrace();
        }
    }
}
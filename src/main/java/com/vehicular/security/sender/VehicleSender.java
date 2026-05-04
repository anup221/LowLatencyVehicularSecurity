package com.vehicular.security.sender;

import com.vehicular.security.crypto.ChaCha20Poly1305Util;
import com.vehicular.security.model.SecurePacket;
import com.vehicular.security.util.KeyManager;
import com.vehicular.security.util.NetworkConfig;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Scanner;

public class VehicleSender {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.println("Enter Vehicle ID:");
            String vehicleId = scanner.nextLine();

            System.out.println("Enter Alert Type (Accident/Traffic/Roadblock):");
            String alertType = scanner.nextLine();

            System.out.println("Enter Emergency Message:");
            String message = scanner.nextLine();

            byte[] nonce = ChaCha20Poly1305Util.generateNonce();

            long startTime = System.currentTimeMillis();

            byte[] encryptedMessage = ChaCha20Poly1305Util.encrypt(
                    KeyManager.getSharedKey(),
                    nonce,
                    message
            );

            SecurePacket packet = new SecurePacket(
                    vehicleId,
                    LocalDateTime.now().toString(),
                    alertType,
                    encryptedMessage,
                    nonce
            );

            Socket socket = new Socket(NetworkConfig.RECEIVER_IP, NetworkConfig.PORT);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());

            oos.writeObject(packet);

            long endTime = System.currentTimeMillis();

            System.out.println("Secure Packet Sent Successfully!");
            System.out.println("Transmission Time: " + (endTime - startTime) + " ms");

            oos.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
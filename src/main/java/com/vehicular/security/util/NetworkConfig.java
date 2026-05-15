package com.vehicular.security.util;

import java.util.Arrays;
import java.util.List;

public class NetworkConfig {

    // Replace with Receiver Laptop IP
    public static final String RECEIVER_IP = "127.0.0.1";

    public static final int PORT = 5000;

    // IP Whitelist: Only these IP addresses are allowed to establish a DH Key Exchange
    public static final List<String> AUTHORIZED_VEHICLE_IPS = Arrays.asList(
            "127.0.0.1",
            "192.168.1.55" // Add sender's Wi-Fi IP here
    );
}
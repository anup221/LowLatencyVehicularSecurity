package com.vehicular.security.model;

import java.io.Serializable;

public class SecurePacket implements Serializable {

    private String vehicleId;
    private String timestamp;
    private String alertType;

    private byte[] cipherText;
    private byte[] nonce;

    public SecurePacket(String vehicleId, String timestamp, String alertType,
                        byte[] cipherText, byte[] nonce) {
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.alertType = alertType;
        this.cipherText = cipherText;
        this.nonce = nonce;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getAlertType() {
        return alertType;
    }

    public byte[] getCipherText() {
        return cipherText;
    }

    public byte[] getNonce() {
        return nonce;
    }
}
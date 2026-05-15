package com.vehicular.security.model;

import java.io.Serializable;

public class SecurePacket implements Serializable {

    private final String vehicleId;
    private final String alertType;
    private final byte[] encryptedMessage;
    private final byte[] nonce;

    public SecurePacket(String vehicleId, String alertType, byte[] encryptedMessage, byte[] nonce) {
        this.vehicleId = vehicleId;
        this.alertType = alertType;
        this.encryptedMessage = encryptedMessage;
        this.nonce = nonce;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public String getAlertType() {
        return alertType;
    }

    public byte[] getEncryptedMessage() {
        return encryptedMessage;
    }

    public byte[] getNonce() {
        return nonce;
    }
}
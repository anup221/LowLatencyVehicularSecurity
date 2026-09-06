# Low-Latency Secure Vehicular Communication

A Java-based V2V communication simulator focused on secure and low-latency message exchange between vehicles.

## Features

- TCP socket-based vehicle communication
- ECDH for dynamic shared-key establishment
- SHA-256 for session-key derivation
- ChaCha20-Poly1305 authenticated encryption
- Random nonce for every encrypted message
- Multi-client receiver using threads
- IP-based access control
- Ciphertext tampering detection

## System Architecture

```text
Vehicle A
   |
   | ECDH public key exchange
   v
Shared Secret
   |
   | SHA-256
   v
256-bit Session Key
   |
   | ChaCha20-Poly1305
   v
Encrypted Message + Nonce + Tag
   |
   | TCP
   v
Vehicle B
   |
   | Verify tag
   v
Decrypt / Reject
```

## Secure Communication Flow

1. Both vehicles generate an ECDH key pair.
2. They exchange public keys and calculate the same shared secret.
3. SHA-256 derives a 256-bit session key from the shared secret.
4. The sender generates a fresh random nonce.
5. ChaCha20-Poly1305 encrypts the message and creates an authentication tag.
6. The encrypted packet is sent through TCP.
7. The receiver verifies the authentication tag before decrypting.
8. If the packet was modified, authentication fails and the packet is rejected.

## Cryptography

### ECDH

ECDH allows two vehicles to establish a shared secret without directly sending the secret over the network.

The project uses the derived secret only to create the session key. Private keys are never transmitted.

> Note: Plain ECDH does not provide identity authentication. A production system should use authenticated ECDHE with certificates, signatures, or PKI to prevent man-in-the-middle attacks.

### SHA-256

SHA-256 converts the ECDH shared secret into the 256-bit key used by ChaCha20-Poly1305.

### ChaCha20-Poly1305

ChaCha20 provides encryption, while Poly1305 provides authentication.

For every message:

```text
Plaintext
   |
   v
ChaCha20-Poly1305
   |
   +--> Ciphertext
   +--> Authentication Tag
   +--> Nonce
```

The nonce is not secret, but it must not be reused with the same key.

If an attacker changes the ciphertext but cannot generate a valid tag using the session key, the receiver rejects the packet.

## SecurePacket

`SecurePacket` carries the information required for communication, including:

- Encrypted payload
- Nonce
- Authentication tag
- Sender/receiver information as defined by the implementation

## Network Communication

The sender uses `Socket` and the receiver uses `ServerSocket`.

The receiver can handle multiple vehicle connections using separate threads. TCP provides reliable and ordered delivery, while ChaCha20-Poly1305 provides application-level confidentiality and integrity.

## Tampering Simulation

`TamperSimulation` demonstrates what happens when encrypted data is modified.

```text
Sender
   |
   v
Ciphertext + Tag
   |
   v
Attacker modifies ciphertext
   |
   v
Receiver
   |
   v
Authentication fails
   |
   v
Packet rejected
```

This simulation demonstrates tamper detection. It is not a full live man-in-the-middle implementation.

## Project Structure

```text
src/main/java/com/vehicular/security/
├── attack/
│   └── TamperSimulation.java
├── crypto/
│   ├── ChaCha20Poly1305Util.java
│   ├── ECDHKeyExchange.java
│   └── SessionKeyManager.java
├── model/
│   └── SecurePacket.java
├── receiver/
│   └── VehicleReceiver.java
├── sender/
│   └── VehicleSender.java
└── util/
    └── NetworkConfig.java
```

## Technologies

- Java
- Maven
- TCP Sockets
- ECDH
- SHA-256
- ChaCha20-Poly1305
- Multithreading

## How to Run

### 1. Build the project

```bash
mvn clean package
```

### 2. Start the receiver

Run:

```text
VehicleReceiver
```

### 3. Start the sender

Run:

```text
VehicleSender
```

The sender establishes a session with the receiver and sends an encrypted V2V message.

### 4. Run the tampering demo

Run:

```text
TamperSimulation
```

The modified packet should fail authentication and be rejected.

## Local Testing

Multiple vehicles can be simulated on the same computer using different processes or terminals and `localhost`.

For testing across computers, configure the receiver IP address and port in `NetworkConfig`.

## Security Properties

- **Confidentiality:** encrypted messages cannot be read without the session key.
- **Integrity:** modified ciphertext fails authentication.
- **Freshness:** each message uses a new nonce.
- **Dynamic keys:** session keys are derived through ECDH instead of using a static symmetric key.
- **Access control:** IP allow-listing restricts accepted connections.

## Limitations and Future Improvements

The current project is a simulator and uses plain ECDH. For a production vehicular network, it should be extended with:

- Authenticated ECDHE
- Digital signatures or certificates
- PKI-based vehicle identity
- Replay protection
- Message timestamps or sequence numbers
- UDP/V2X support where appropriate



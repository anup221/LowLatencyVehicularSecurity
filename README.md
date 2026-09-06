# Low-Latency Secure Vehicular Communication

A Java-based real-time vehicular communication security simulator designed to demonstrate secure Vehicle-to-Vehicle (V2V) communication using dynamic ECDH key exchange, SHA-256 key derivation, ChaCha20-Poly1305 authenticated encryption, TCP socket communication, multi-client handling, IP-based access control, and ciphertext tampering detection.

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [System Architecture](#system-architecture)
- [Secure Communication Flow](#secure-communication-flow)
- [ECDH Key Exchange](#ecdh-key-exchange)
- [Session Key Derivation](#session-key-derivation)
- [ChaCha20-Poly1305 Encryption](#chacha20-poly1305-encryption)
- [ChaCha20 Internals](#chacha20-internals)
- [Poly1305 Authentication](#poly1305-authentication)
- [SecurePacket](#securepacket)
- [TCP Communication](#tcp-communication)
- [Real-Time Vehicular Communication](#real-time-vehicular-communication)
- [Multi-Vehicle Simulation](#multi-vehicle-simulation)
- [Network Access Control](#network-access-control)
- [Tampering Simulation](#tampering-simulation)
- [Authentication Failure](#authentication-failure)
- [Attack Demonstration](#attack-demonstration)
- [Attacker Visibility](#attacker-visibility)
- [Complete Security Flow](#complete-security-flow)
- [Project Structure](#project-structure)
- [Module Responsibilities](#module-responsibilities)
- [Technology Stack](#technology-stack)
- [How to Run](#how-to-run)
- [Running Multiple Vehicles](#running-multiple-vehicles)
- [Tampering Demo](#tampering-demo)
- [Security Properties](#security-properties)
- [Security Model](#security-model)
- [Design Limitation](#design-limitation)
- [Why These Technologies](#why-these-technologies)
- [Future Improvements](#future-improvements)
- [Learning Outcomes](#learning-outcomes)
- [Example End-to-End Scenario](#example-end-to-end-scenario)
- [Resume Description](#resume-description)

---

# Overview

Vehicular networks require fast and secure communication because vehicles may need to exchange safety-critical information such as:

- Accident warnings
- Emergency braking alerts
- Traffic congestion information
- Road hazard notifications
- Collision warnings
- Vehicle status information

This project simulates secure Vehicle-to-Vehicle (V2V) communication using Java.

The system establishes a dynamic session key between communicating vehicles using Elliptic Curve Diffie-Hellman (ECDH). The derived session key is then used with ChaCha20-Poly1305 to provide both confidentiality and integrity.

Messages are transmitted through TCP sockets and packaged into a `SecurePacket`.

The project also contains a tampering simulation that demonstrates how modified ciphertext is detected and rejected by the receiver.

---

# Features

- Real-time TCP-based communication
- Vehicle-to-Vehicle communication simulation
- Dynamic ECDH key exchange
- SHA-256 session-key derivation
- 256-bit symmetric session keys
- ChaCha20-Poly1305 authenticated encryption
- Random nonce generation for every encrypted message
- Authentication tag verification
- Secure packet structure
- Multi-client receiver support
- IP-based access control
- Ciphertext tampering simulation
- Automatic rejection of modified packets
- Localhost-based multi-vehicle simulation
- No static symmetric encryption key required

---

# System Architecture

```text
                 VEHICLE A
                     |
                     |
              Generate ECDH Key Pair
                     |
                     |
                Public Key
                     |
                     v
               TCP Connection
                     |
                     |
                     v
                 VEHICLE B
                     |
              Generate ECDH Key Pair
                     |
                     |
                Public Key
                     |
                     v
              ECDH Shared Secret
                     |
                     v
                 SHA-256
                     |
                     v
            256-bit Session Key
                     |
                     v
              Message Encryption
                     |
                     v
          ChaCha20-Poly1305
                     |
              +------+------+
              |             |
          Ciphertext      Tag
              |             |
              +------+------+
                     |
                     v
                SecurePacket
                     |
                     v
                    TCP
                     |
                     v
                 VEHICLE B
                     |
                     v
           Authentication Check
                     |
              +------+------+
              |             |
            VALID         INVALID
              |             |
              v             v
          Decrypt        Reject
              |
              v
        Original Message

# Secure Communication Flow

The communication process can be divided into several stages.

## Step 1: Generate ECDH Key Pairs

Each vehicle generates its own ECDH key pair.

Each key pair contains:

```
```

```
Private Key
Public Key
```

The private key remains secret.

The public key can be exchanged with the other vehicle.

---

## Step 2: Exchange Public Keys

Vehicle A sends its public key to Vehicle B.

Vehicle B sends its public key to Vehicle A.

The private keys are never transmitted.

```
```

```
Vehicle A                         Vehicle B

Private A                         Private B
   |                                  |
   |                                  |
Public A --------->                  |
   |                                  |
   |                  <--------- Public B
   |                                  |
```

---

# ECDH Key Exchange

ECDH stands for:

**Elliptic Curve Diffie-Hellman**

It is a key agreement algorithm that allows two parties to establish a shared secret without directly transmitting that secret over the network.

Each vehicle has:

```
```

```
Vehicle A:
Private Key A
Public Key A

Vehicle B:
Private Key B
Public Key B
```

The public keys are exchanged.

Vehicle A computes:

```
```

```
Shared Secret = ECDH(Private Key A, Public Key B)
```

Vehicle B computes:

```
```

```
Shared Secret = ECDH(Private Key B, Public Key A)
```

Both operations result in the same shared secret.

```
```

```
             Public Key A
                   |
                   v
Private Key B ---> ECDH ---> Shared Secret

Private Key A ---> ECDH ---> Shared Secret
                   ^
                   |
             Public Key B
```

The shared secret itself does not need to be transmitted.

---

# Session Key Derivation

The raw ECDH shared secret is passed through SHA-256.

```
```

```
ECDH Shared Secret
        |
        v
     SHA-256
        |
        v
256-bit Session Key
```

SHA-256 produces a 256-bit output.

This output is used as the symmetric session key for ChaCha20-Poly1305.

The project therefore follows:

```
```

```
ECDH
 ↓
Shared Secret
 ↓
SHA-256
 ↓
256-bit Session Key
 ↓
ChaCha20-Poly1305
```

The session key is dynamically derived rather than using a hard-coded static encryption key.

---

# ChaCha20-Poly1305 Encryption

ChaCha20-Poly1305 is an authenticated encryption construction.

It provides two important security properties:

### Confidentiality

An attacker should not be able to read the original message.

This is provided by:

```
```

```
ChaCha20
```

### Integrity and Authentication

An attacker should not be able to modify the encrypted message without detection.

This is provided by:

```
```

```
Poly1305
```

Therefore:

```
```

```
ChaCha20  -> Encryption
Poly1305  -> Authentication
```

Together:

```
```

```
ChaCha20-Poly1305
        |
        +---- Confidentiality
        |
        +---- Integrity
        |
        +---- Authentication
```

---

# Encryption Process

Suppose Vehicle A wants to send:

```
```

```
Accident Ahead near Junction 5
```

The process is:

```
```

```
Plaintext
   |
   v
Generate Random Nonce
   |
   v
ChaCha20 Encryption
   |
   v
Ciphertext
   |
   +
   |
   v
Poly1305 Authentication
   |
   v
Authentication Tag
```

The transmitted packet contains information such as:

```
```

```
Nonce
Ciphertext
Authentication Tag
```

The session key is not transmitted with the packet.

---

# Nonce

A nonce is a number used once.

ChaCha20-Poly1305 requires a unique nonce for each encryption under the same key.

The nonce does not have to be secret.

Therefore:

```
```

```
Nonce = Public
Session Key = Secret
```

The project generates a fresh random nonce for encrypted messages.

The receiver uses the nonce received with the packet to reproduce the required cryptographic operation.

The important rule is:

```
```

```
Same key + same nonce
```

must not be reused for separate encryption operations.

---

# ChaCha20 Internals

ChaCha20 is a stream cipher.

It does not directly encrypt the plaintext using a traditional block-by-block substitution method.

Instead, it generates a pseudorandom keystream.

The keystream is then XORed with the plaintext.

```
```

```
Plaintext
    XOR
Keystream
    |
    v
Ciphertext
```

For decryption:

```
```

```
Ciphertext
    XOR
Same Keystream
    |
    v
Plaintext
```

Because:

```
```

```
A XOR B XOR B = A
```

the same keystream can recover the original plaintext.

---

# ChaCha20 Inputs

ChaCha20 uses:

```
```

```
256-bit Key
96-bit Nonce
32-bit Block Counter
```

These values are used to construct the ChaCha20 internal state.

The state contains 16 words of 32 bits each.

Conceptually:

```
```

```
+----------------+----------------+----------------+----------------+
| Constant       | Constant       | Constant       | Constant       |
+----------------+----------------+----------------+----------------+
| Key            | Key            | Key            | Key            |
+----------------+----------------+----------------+----------------+
| Key            | Key            | Key            | Key            |
+----------------+----------------+----------------+----------------+
| Counter        | Nonce          | Nonce          | Nonce          |
+----------------+----------------+----------------+----------------+
```

---

# ARX Operations

ChaCha20 is based on ARX operations.

ARX stands for:

```
```

```
A = Addition
R = Rotation
X = XOR
```

More specifically:

### Addition

32-bit addition modulo:

```
```

```
2^32
```

### XOR

Exclusive OR operation.

### Rotation

Bits are rotated left by a fixed number of positions.

These operations are combined to create strong diffusion and mixing.

---

# ChaCha20 Quarter Round

A simplified ChaCha20 quarter round is:

```
```

```
a += b
d ^= a
d = ROTL(d, 16)

c += d
b ^= c
b = ROTL(b, 12)

a += b
d ^= a
d = ROTL(d, 8)

c += d
b ^= c
b = ROTL(b, 7)
```

The complete ChaCha20 block function applies these operations repeatedly.

ChaCha20 uses 20 rounds, arranged as 10 double rounds.

The repeated mixing makes the resulting keystream computationally unpredictable.

---

# Why XOR Is Used

Suppose a plaintext byte is:

```
```

```
10110010
```

and the keystream byte is:

```
```

```
01101001
```

XOR gives:

```
```

```
10110010
01101001
--------
11011011
```

The resulting value:

```
```

```
11011011
```

is part of the ciphertext.

During decryption:

```
```

```
11011011
01101001
--------
10110010
```

The original plaintext is recovered.

---

# Poly1305 Authentication

ChaCha20 alone provides encryption but does not provide authentication.

Poly1305 provides message authentication.

During encryption, an authentication tag is generated.

Conceptually:

```
```

```
Ciphertext
     |
     v
 Poly1305
     |
     v
Authentication Tag
```

The tag is transmitted along with the encrypted packet.

The receiver independently verifies the tag.

---

# Authentication Verification

When the receiver gets a packet:

```
```

```
Nonce
Ciphertext
Authentication Tag
```

it uses the session key and received data to verify the authentication tag.

If the packet has not been modified:

```
```

```
Calculated Tag
      =
Received Tag

      |
      v

VALID
```

The receiver can then decrypt the ciphertext.

If the ciphertext has been modified:

```
```

```
Calculated Tag
      !=
Received Tag

      |
      v

INVALID
```

The packet is rejected.

This prevents an attacker from silently modifying safety-critical vehicular messages.

---

# SecurePacket

The project uses a `SecurePacket` model to represent encrypted communication data.

Conceptually, a secure packet contains:

```
```

```
SecurePacket
│
├── Nonce
├── Ciphertext
└── Authentication Tag
```

The packet is transmitted through the TCP connection.

The receiver extracts the required cryptographic information and verifies the packet before accepting the message.

---

# TCP Communication

The project uses TCP sockets for communication between vehicles.

The sender uses:

```
```

```
Socket
```

The receiver uses:

```
```

```
ServerSocket
```

The basic flow is:

```
```

```
Vehicle Sender
      |
      | TCP Connection
      v
Vehicle Receiver
```

TCP provides:

-  Reliable delivery 
-  Ordered delivery 
-  Connection-oriented communication 
-  Retransmission of lost TCP segments 

However, TCP itself does not provide application-level encryption.

Therefore, the project encrypts the message using ChaCha20-Poly1305 before sending it over TCP.

```
```

```
Application Message
       |
       v
ChaCha20-Poly1305
       |
       v
Encrypted Data
       |
       v
TCP
       |
       v
Receiver
```

---

# Real-Time Vehicular Communication

The system is designed to simulate real-time V2V communication.

For example:

```
```

```
Vehicle A
   |
   | "Accident Ahead near Junction 5"
   |
   v
Encryption
   |
   v
TCP Transmission
   |
   v
Vehicle B
   |
   v
Decryption
   |
   v
"Accident Ahead near Junction 5"
```

This represents a simplified emergency warning system.

---

# Multi-Vehicle Simulation

The receiver supports multiple client connections.

Each incoming connection can be handled using a separate thread.

Conceptually:

```
```

```
                    Vehicle Receiver
                          |
              +-----------+-----------+
              |           |           |
              v           v           v
          Vehicle A   Vehicle B   Vehicle C
          Thread 1    Thread 2    Thread 3
```

This allows the system to simulate multiple vehicles communicating with a central receiver or communication endpoint.

---

# Network Access Control

The project includes IP-based access control.

The receiver can check whether a connecting IP address is permitted.

Conceptually:

```
```

```
Incoming Connection
        |
        v
Check IP Address
        |
    +---+---+
    |       |
Allowed   Blocked
    |       |
    v       v
Accept    Reject
```

This provides an additional network-level access-control layer.

---

# Tampering Simulation

The project includes `TamperSimulation.java` to demonstrate what happens when an attacker modifies encrypted data.

The attack scenario is:

```
```

```
Original Message
       |
       v
Encryption
       |
       v
Ciphertext + Authentication Tag
       |
       v
Attacker
       |
       | Modify Ciphertext
       v
Modified Ciphertext
       |
       v
Receiver
```

The attacker does not know the legitimate session key.

Therefore, simply changing the ciphertext while keeping the original authentication tag causes verification to fail.

---

# Authentication Failure

Suppose the legitimate sender generates:

```
```

```
Ciphertext = C
Tag = T
```

An attacker changes the ciphertext:

```
```

```
C -> C'
```

but keeps:

```
```

```
Tag = T
```

The receiver calculates a new authentication value for the modified ciphertext.

The result will not match the original tag.

```
```

```
Calculated Tag != Received Tag
```

Therefore:

```
```

```
Packet Rejected
```

The modified message is not accepted as authentic.

---

# Attack Demonstration

The tampering process can be visualized as:

```
```

```
              LEGITIMATE COMMUNICATION

Sender
  |
  | Plaintext
  v
Encryption
  |
  | Ciphertext + Tag
  v
-------------------------
        Network
-------------------------
  |
  v
Receiver
  |
  v
Verification
  |
  v
Decryption
```

During tampering:

```
```

```
Sender
  |
  v
Ciphertext + Tag
  |
  v
Attacker
  |
  | Modify Ciphertext
  v
Modified Ciphertext + Original Tag
  |
  v
Receiver
  |
  v
Authentication Verification
  |
  v
FAILED
  |
  v
Packet Rejected
```

---

# Attacker Visibility

An attacker monitoring the network may be able to observe:

```
```

```
IP Address
Port
Packet Size
Nonce
Ciphertext
Authentication Tag
```

However, the attacker should not be able to recover the plaintext without the secret session key.

The nonce is not secret.

The session key is secret.

Therefore:

```
```

```
Nonce       -> Public
Ciphertext  -> Public
Tag         -> Public
Session Key -> Secret
Private Key -> Secret
```

---

# Why Cannot the Attacker Simply Modify the Ciphertext?

Suppose an attacker modifies:

```
```

```
Ciphertext
```

The attacker also needs to create a valid authentication tag for the modified ciphertext.

That requires the secret cryptographic key.

Without the key:

```
```

```
Modified Ciphertext
        +
Invalid Tag
        |
        v
Receiver
        |
        v
Authentication Failure
```

Therefore, the receiver rejects the packet.

This is the main integrity property demonstrated by the tampering simulation.

---

# Complete Security Flow

The complete system works as follows:

```
```

```
                VEHICLE A
                    |
                    v
          Generate ECDH Key Pair
                    |
                    v
             Exchange Public Key
                    |
                    v
          ECDH Shared Secret
                    |
                    v
                SHA-256
                    |
                    v
           Session Key
                    |
                    v
             Create Message
                    |
                    v
          Generate Random Nonce
                    |
                    v
          ChaCha20 Encryption
                    |
                    v
               Ciphertext
                    |
                    v
          Poly1305 Authentication
                    |
                    v
          Authentication Tag
                    |
                    v
              SecurePacket
                    |
                    v
                   TCP
                    |
                    v
                VEHICLE B
                    |
                    v
          Receive SecurePacket
                    |
                    v
       Verify Authentication Tag
                    |
             +------+------+
             |             |
           VALID         INVALID
             |             |
             v             v
          Decrypt        Reject
             |
             v
       Original Message
```

---

# Project Structure

```
```

```
LowLatencyVehicularSecurity/
│
├── pom.xml
│
└── src/
    └── main/
        └── java/
            └── com/
                └── vehicular/
                    └── security/
                        │
                        ├── attack/
                        │   └── TamperSimulation.java
                        │
                        ├── crypto/
                        │   ├── ChaCha20Poly1305Util.java
                        │   ├── ECDHKeyExchange.java
                        │   └── SessionKeyManager.java
                        │
                        ├── model/
                        │   └── SecurePacket.java
                        │
                        ├── receiver/
                        │   └── VehicleReceiver.java
                        │
                        ├── sender/
                        │   └── VehicleSender.java
                        │
                        └── util/
                            └── NetworkConfig.java
```

---

# Module Responsibilities

| FileResponsibility          |                                                                        |
| --------------------------- | ---------------------------------------------------------------------- |
| `ECDHKeyExchange.java`      | Generates ECDH key pairs and performs ECDH shared-secret establishment |
| `SessionKeyManager.java`    | Derives the symmetric session key using SHA-256                        |
| `ChaCha20Poly1305Util.java` | Performs ChaCha20-Poly1305 encryption and decryption                   |
| `SecurePacket.java`         | Represents encrypted packet information                                |
| `VehicleSender.java`        | Creates and sends secure vehicular messages                            |
| `VehicleReceiver.java`      | Receives, verifies, decrypts, and processes packets                    |
| `TamperSimulation.java`     | Simulates ciphertext modification and authentication failure           |
| `NetworkConfig.java`        | Stores network-related configuration                                   |

---

# Technology Stack

| TechnologyPurpose |                                   |
| ----------------- | --------------------------------- |
| Java              | Main programming language         |
| Maven             | Project and dependency management |
| TCP Sockets       | Network communication             |
| ECDH              | Dynamic key agreement             |
| SHA-256           | Session-key derivation            |
| ChaCha20          | Encryption                        |
| Poly1305          | Authentication and integrity      |
| SecureRandom      | Random nonce generation           |
| Multithreading    | Multiple vehicle/client handling  |

---

# How to Run

## Prerequisites

Install:

-  Java JDK 
-  Maven 
-  An IDE such as IntelliJ IDEA or Eclipse 

Verify Java:

```
```

```
java -version
```

Verify Maven:

```
```

```
mvn -version
```

---

# Build the Project

From the project root:

```
```

```
mvn clean compile
```

If the build succeeds, the project is ready to run.

---

# Running the Receiver

Start the receiver first.

Run:

```
```

```
VehicleReceiver.java
```

The receiver starts a TCP server and waits for vehicle connections.

Conceptually:

```
```

```
Receiver
   |
   v
ServerSocket
   |
   v
Waiting for connections...
```

---

# Running the Sender

After starting the receiver, run:

```
```

```
VehicleSender.java
```

The sender establishes a TCP connection with the receiver and performs the required key exchange before sending encrypted messages.

The communication flow is:

```
```

```
Sender
  |
  | Connect
  v
Receiver
  |
  | ECDH Key Exchange
  v
Session Key Established
  |
  v
Encrypt Message
  |
  v
Send SecurePacket
```

---

# Running Multiple Vehicles

Multiple sender instances can be started to simulate multiple vehicles.

For example:

```
```

```
Vehicle A
Vehicle B
Vehicle C
        |
        v
Vehicle Receiver
```

On one machine, this can be simulated using:

```
```

```
127.0.0.1
```

Each vehicle can run as a separate Java process or terminal/IDE window.

---

# Localhost Simulation

For testing everything on one computer:

```
```

```
Vehicle A
   |
   | 127.0.0.1
   v
Receiver
   ^
   | 127.0.0.1
   |
Vehicle B
```

Running separate processes allows the system to simulate independent vehicles.

---

# Running Across Two Computers

The same project can also be tested across two computers connected to the same network.

For example:

```
```

```
Computer 1
Private IP: 192.168.1.10
        |
        | Wi-Fi / LAN
        |
        v
Computer 2
Private IP: 192.168.1.20
```

The receiver runs on one computer.

The sender connects to the receiver's local network IP address.

The firewall must allow the selected TCP port.

---

# Tampering Demo

The project includes a tampering simulation.

The basic attack is:

```
```

```
Original Ciphertext
       |
       v
Attacker modifies data
       |
       v
Modified Ciphertext
       |
       v
Receiver
       |
       v
Authentication Verification
       |
       v
FAILED
       |
       v
Packet Rejected
```

The important observation is that the attacker can modify transmitted bytes, but cannot create a valid authentication tag without the secret session key.

---

# Expected Security Behavior

### Normal Communication

```
```

```
Message
   |
   v
Encrypt
   |
   v
Transmit
   |
   v
Verify
   |
   v
Decrypt
   |
   v
Accepted
```

### Tampered Communication

```
```

```
Message
   |
   v
Encrypt
   |
   v
Transmit
   |
   v
Attacker modifies ciphertext
   |
   v
Verify
   |
   v
FAILED
   |
   v
Rejected
```

---

# Security Properties

## Confidentiality

ChaCha20 encrypts the plaintext.

An attacker observing the network sees ciphertext rather than the original message.

```
```

```
Plaintext
   |
   v
ChaCha20
   |
   v
Ciphertext
```

---

## Integrity

Poly1305 detects unauthorized modifications.

If ciphertext changes, authentication verification fails.

```
```

```
Original Data
     |
     v
Valid Tag

Modified Data
     |
     v
Invalid Tag
```

---

## Authentication

The authentication tag allows the receiver to verify that the encrypted data has not been modified.

A modified packet should not be accepted as a valid message.

---

## Dynamic Session Keys

The project does not rely on a hard-coded static symmetric encryption key.

Instead:

```
```

```
ECDH
 ↓
Shared Secret
 ↓
SHA-256
 ↓
Session Key
```

The symmetric encryption key is derived dynamically.

---

## Nonce-Based Encryption

A fresh nonce is generated for encrypted messages.

The nonce does not need to be secret.

Its main requirement is uniqueness for a given key.

---

# Security Model

The project assumes an attacker may be able to observe or modify network traffic.

The attacker may see:

```
```

```
Encrypted Packet
Nonce
Ciphertext
Authentication Tag
```

The attacker should not know:

```
```

```
Private ECDH Key
Session Key
```

Therefore, an attacker can attempt:

```
```

```
Modify Ciphertext
```

but cannot produce a valid authentication tag for the modified ciphertext without the required secret key.

---

# Design Limitation

The current project demonstrates ECDH key agreement, but **plain unauthenticated ECDH does not by itself prevent a Man-in-the-Middle (MITM) attack**.

ECDH establishes a shared secret, but the communicating parties also need a way to verify that the exchanged public key actually belongs to the intended vehicle.

For example:

```
```

```
Vehicle A
    |
    | Public Key
    v
Vehicle B
```

Without authentication, an attacker could potentially intercept and replace public keys.

A production vehicular security system should therefore use authenticated key exchange.

Possible improvements include:

-  Authenticated ECDHE 
-  Digital signatures 
-  Vehicle certificates 
-  Public Key Infrastructure (PKI) 
-  Certificate authorities 
-  Secure vehicle identities 

---

# Important Distinction About TamperSimulation

`TamperSimulation.java` demonstrates **ciphertext tampering and authentication failure**.

It should not be described as a complete live Man-in-the-Middle attack.

The demonstrated attack is:

```
```

```
Valid Ciphertext
       |
       v
Attacker modifies ciphertext
       |
       v
Receiver verifies tag
       |
       v
Verification fails
```

A full MITM implementation would require the attacker to actively sit between the two endpoints and interfere with the key exchange and communication session.

---

# Why These Technologies

## Why ECDH?

ECDH allows two vehicles to establish a shared secret without transmitting the secret itself.

Advantages:

-  Efficient key agreement 
-  Smaller keys compared with traditional finite-field DH 
-  Suitable for modern secure communication 
-  Provides a foundation for dynamic session keys 

---

## Why SHA-256?

SHA-256 is used to derive a fixed-size key from the ECDH shared secret.

```
```

```
ECDH Shared Secret
        |
        v
      SHA-256
        |
        v
  256-bit Key
```

This gives the encryption layer a consistent 256-bit key.

---

## Why ChaCha20?

ChaCha20 is a modern stream cipher based on ARX operations.

It is efficient in software and is widely used in modern cryptographic systems.

---

## Why Poly1305?

Encryption alone does not guarantee that ciphertext has not been modified.

Poly1305 provides authentication and integrity protection.

Therefore:

```
```

```
ChaCha20 -> Confidentiality
Poly1305 -> Integrity + Authentication
```

---

## Why TCP?

TCP provides:

-  Reliable transmission 
-  Ordered delivery 
-  Connection-oriented communication 
-  Retransmission 

This makes it useful for the communication simulation.

The cryptographic layer is implemented separately because TCP itself does not provide application-level encryption.

---

# Future Improvements

The project can be extended with:

## 1. Authenticated ECDH

Add digital signatures or certificates so vehicles can verify each other's public keys.

---

## 2. Vehicle Identity

Each vehicle can have a cryptographically verified identity.

```
```

```
Vehicle ID
    |
    v
Certificate
    |
    v
Public Key
```

---

## 3. PKI

A Public Key Infrastructure could be used to manage vehicle certificates.

---

## 4. Replay Attack Protection

Add sequence numbers or timestamps to detect replayed packets.

For example:

```
```

```
Packet
 ├── Vehicle ID
 ├── Sequence Number
 ├── Timestamp
 ├── Nonce
 ├── Ciphertext
 └── Authentication Tag
```

---

## 5. Key Rotation

Session keys could periodically be regenerated to reduce the amount of data encrypted under a single key.

---

## 6. Performance Measurement

The system could record:

-  Encryption time 
-  Decryption time 
-  Key exchange time 
-  Network latency 
-  Throughput 
-  Packet processing time 

This would allow the project to better demonstrate its low-latency objective.

---

## 7. Replay Attack Simulation

An attacker could capture a valid encrypted packet and retransmit it.

The receiver could detect this using:

```
```

```
Sequence Number
+
Timestamp
+
Nonce
```

---

## 8. Full MITM Simulation

A future version could implement an actual proxy between two vehicles:

```
```

```
Vehicle A
    |
    v
Attacker / MITM
    |
    v
Vehicle B
```

The MITM could attempt to intercept the key exchange and demonstrate why authenticated ECDH is necessary.

---

# Learning Outcomes

This project demonstrates practical knowledge of:

-  Java networking 
-  TCP socket programming 
-  Multithreading 
-  Cryptography 
-  ECDH key exchange 
-  SHA-256 
-  Symmetric encryption 
-  ChaCha20 
-  Poly1305 
-  AEAD encryption 
-  Nonce management 
-  Authentication tags 
-  Secure packet design 
-  Network access control 
-  Attack simulation 
-  Integrity verification 
-  Secure communication architecture 

---

# Example End-to-End Scenario

Suppose Vehicle A detects an accident.

It wants to send:

```
```

```
Accident Ahead near Junction 5
```

## Step 1

Vehicle A generates an ECDH key pair.

```
```

```
Private A
Public A
```

## Step 2

Vehicle B generates:

```
```

```
Private B
Public B
```

## Step 3

The public keys are exchanged.

```
```

```
Public A <------> Public B
```

## Step 4

Both vehicles independently calculate:

```
```

```
ECDH Shared Secret
```

Both obtain the same shared secret.

## Step 5

The shared secret is passed through SHA-256.

```
```

```
ECDH Shared Secret
        |
        v
     SHA-256
        |
        v
256-bit Session Key
```

## Step 6

Vehicle A creates:

```
```

```
Accident Ahead near Junction 5
```

## Step 7

A random nonce is generated.

## Step 8

ChaCha20 encrypts the plaintext.

```
```

```
Plaintext
    +
Session Key
    +
Nonce
    |
    v
ChaCha20
    |
    v
Ciphertext
```

## Step 9

Poly1305 generates the authentication tag.

```
```

```
Ciphertext
     |
     v
Poly1305
     |
     v
Authentication Tag
```

## Step 10

The sender creates a secure packet.

```
```

```
SecurePacket
 ├── Nonce
 ├── Ciphertext
 └── Authentication Tag
```

## Step 11

The packet is sent through TCP.

## Step 12

Vehicle B receives the packet.

## Step 13

Vehicle B verifies the authentication tag.

If valid:

```
```

```
Verify
  |
  v
Decrypt
  |
  v
Accident Ahead near Junction 5
```

If invalid:

```
```

```
Verify
  |
  v
FAIL
  |
  v
Reject Packet
```

---

# Resume Description

## Short Version

**Low-Latency Secure Vehicular Communication** — Built a Java-based real-time V2V communication simulator using TCP sockets, ECDH dynamic session-key establishment, SHA-256 key derivation, and ChaCha20-Poly1305 authenticated encryption.

---

## Detailed Version

**Low-Latency Secure Vehicular Communication** — Developed a Java-based real-time V2V security simulator using TCP socket programming and dynamic ECDH session-key establishment; implemented SHA-256 key derivation, ChaCha20-Poly1305 authenticated encryption, per-message nonce generation, multi-client communication, IP-based access control, and ciphertext-tampering detection with automatic packet rejection.

---

# Resume Keywords

```
```

```
Java
TCP Socket Programming
Computer Networks
Cryptography
ECDH
SHA-256
ChaCha20-Poly1305
Secure Communication
V2V Communication
Multithreading
Network Security
Authentication
Integrity Verification
```

---

# Conclusion

This project demonstrates how modern cryptographic techniques can be combined with network programming to build a secure vehicular communication system.

The core security architecture is:

```
```

```
ECDH
  |
  v
Shared Secret
  |
  v
SHA-256
  |
  v
256-bit Session Key
  |
  v
ChaCha20-Poly1305
  |
  +---- ChaCha20 -> Confidentiality
  |
  +---- Poly1305 -> Integrity + Authentication
  |
  v
SecurePacket
  |
  v
TCP
  |
  v
Receiver
  |
  v
Authentication Verification
  |
  +------ VALID ------> Decrypt
  |
  +------ INVALID ----> Reject
```

The project therefore provides a practical demonstration of secure V2V communication, dynamic key establishment, authenticated encryption, reliable network transmission, and detection of malicious ciphertext modification.

```
```

```
```

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

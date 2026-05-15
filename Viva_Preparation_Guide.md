# 🎓 Viva Preparation Guide: Real-Time Vehicular Security System

## 1. Project Architecture Overview
**"What did you build?"**
This project is a **Real-Time Vehicular Communication Security Simulation**. It simulates two entities (a Vehicle Sender and a Receiver/Infrastructure) communicating over a TCP/IP network. Instead of relying on hardcoded passwords or pre-shared keys (which are weak and easily stolen), the system uses an **Elliptic Curve Diffie-Hellman (ECDH)** key exchange to dynamically establish a secure session. Once established, all messages (like emergency alerts) are encrypted and authenticated using **ChaCha20-Poly1305**.

### The Core Flow:
1. **Connection & Whitelist Check:** The Receiver listens on a TCP port. When the Sender connects, the Receiver first checks if the Sender's IP is in the `AUTHORIZED_VEHICLE_IPS` whitelist to prevent rogue connections.
2. **Key Exchange (ECDH):** Both parties generate temporary public and private keys. They exchange *only* the public keys over the network and mathematically compute an identical **Shared Secret**.
3. **Session Key Derivation:** The shared secret is passed through a **SHA-256** hash function to generate a 256-bit cryptographic Session Key.
4. **Data Transmission (ChaCha20-Poly1305):** The Sender encrypts the emergency message using the Session Key and a random 12-byte Nonce. Poly1305 attaches a MAC (Message Authentication Code) to the ciphertext.
5. **Decryption & Verification:** The Receiver uses the same Session Key to decrypt it. If an attacker tampers with the ciphertext in transit, Poly1305 detects the modification and rejects the packet immediately.

---

## 2. Elliptic Curve Diffie-Hellman (ECDH) Key Exchange
**"How does the Key Exchange work without sharing the private key?"**

ECDH relies on the **Elliptic Curve Discrete Logarithm Problem**—a math problem that is easy to compute in one direction but virtually impossible to reverse.

**Step-by-Step Execution:**
1. **Generation:** The Sender generates `Private_Key_A` and `Public_Key_A`. The Receiver generates `Private_Key_B` and `Public_Key_B`.
2. **Exchange:** They send their *Public Keys* to each other over the unencrypted network. If an attacker is listening, they only see the Public Keys.
3. **The Math (The Magic):**
   - The Sender multiplies: `Private_Key_A * Public_Key_B = Shared_Secret`
   - The Receiver multiplies: `Private_Key_B * Public_Key_A = Shared_Secret`
   - Because of the properties of elliptic curves, both calculations yield the **exact same Shared Secret**.
4. **Security Strength:** The attacker has `Public_Key_A` and `Public_Key_B`, but without knowing either of the Private Keys, they cannot compute the Shared Secret.

---

## 3. ChaCha20 Encryption Algorithm
**"Why ChaCha20 instead of AES? How does it encrypt?"**

ChaCha20 is a **Stream Cipher** (unlike AES, which is a block cipher). It is incredibly fast in software and does not require specialized hardware acceleration, making it highly preferred for low-latency systems like IoT and Vehicular Networks.

**How it works:**
1. **The Inputs:** ChaCha20 takes your 256-bit Session Key, a 12-byte Random Nonce (Number used ONCE), and a counter.
2. **The Matrix:** It loads these inputs into a 4x4 matrix of 32-bit words.
3. **The ChaCha Rounds:** It runs 20 rounds of mathematical operations (Addition, XOR, and Bit-Shifts) on the matrix. This intensely scrambles the data.
4. **The Keystream:** The output of these 20 rounds is a perfectly pseudorandom block of data called a **Keystream**.
5. **Encryption (XOR):** The Keystream is `XOR`ed (exclusive OR) bit-by-bit against your plaintext (the emergency message) to create the ciphertext. To decrypt, the receiver simply generates the exact same Keystream and `XOR`s it against the ciphertext to get the plaintext back!

---

## 4. Poly1305 Authentication (The Tamper Detection)
**"How do you know the attacker didn't change the ciphertext or the Vehicle ID?"**

While ChaCha20 encrypts the data (Confidentiality), **Poly1305** authenticates it (Integrity). They are used together in a mode called **AEAD (Authenticated Encryption with Associated Data)**.

**How AEAD works with the Vehicle ID:**
1. **The Ciphertext:** ChaCha20 encrypts the emergency message into ciphertext.
2. **The Associated Data (AAD):** Some data, like the `Vehicle ID`, needs to remain unencrypted so the Receiver knows who sent the packet, but it still must be protected from tampering. We pass the `Vehicle ID` to Poly1305 as **AAD (Additional Authenticated Data)**.
3. **The MAC Calculation:** Poly1305 calculates a **MAC (Message Authentication Code)** tag. This tag is a mathematical polynomial evaluated over **both the Ciphertext AND the unencrypted Vehicle ID** using the Session Key.
4. **The Verification:** When the Receiver gets the packet, it runs Poly1305 over the ciphertext and the provided Vehicle ID. If the attacker tampered with the ciphertext OR spoofed/changed the `Vehicle ID` in transit, the calculated MAC will not match the original MAC attached to the packet.
5. **The Attack Scenario:** In our `TamperSimulation`, the attacker blindly flips a bit in the ciphertext (`encryptedMessage[0] ^= 1;`). When the Receiver calculates the MAC of this tampered packet, the decryption instantly throws an exception (`Authentication Failed!`), preventing the system from processing corrupted or spoofed data.

## 5. Attacker Simulation Flow (Man-In-The-Middle)
**"How does the attacker operate in our simulation, and why do they fail?"**

Our `TamperSimulation.java` models a realistic **Man-In-The-Middle (MITM)** attack on the wireless network. Here is exactly how the attacker operates and how the cryptography defeats them:

1. **The Interception:** The Sender and Receiver successfully establish an ECDH Session Key. The Sender encrypts a real emergency packet (e.g., "Accident Ahead") and transmits it over the network. The attacker intercepts this packet.
2. **The Attacker's Blindness:** Because ChaCha20 uses a 256-bit key that the attacker does not have, the intercepted packet is completely unreadable ciphertext. The attacker cannot decrypt it, read the contents, or figure out the Session Key.
3. **The Tampering Attempt:** Frustrated by not being able to read the data, the attacker decides to maliciously modify it to cause chaos (e.g., flipping bits to change "Accident Ahead" to "Clear Road"). In the simulation, this is done via a bitwise XOR operation on the first byte of the ciphertext: `encryptedMessage[0] ^= 1;`. The attacker then forwards this corrupted packet to the Receiver.
4. **The Receiver's Detection:** The Receiver accepts the packet and passes it to the ChaCha20-Poly1305 decryption algorithm. Before decrypting, Poly1305 mathematically re-evaluates the MAC tag of the incoming ciphertext. 
5. **The Rejection:** Because the attacker flipped a bit in the ciphertext, the newly calculated MAC completely mismatches the MAC attached by the original Sender. Poly1305 instantly flags the data as tampered/corrupted and throws an `Authentication Failed` exception. The Receiver drops the packet before acting on the false data.

---

### Potential Viva Questions & Answers to Keep in Mind:

* **Q: What happens if an attacker connects to your server?**
  * **A:** Our `NetworkConfig` implements an IP Whitelist. If an unauthorized IP attempts to connect to the TCP socket, the connection is instantly dropped before any key exchange logic occurs.

* **Q: Why hash the shared secret with SHA-256? Why not use it directly?**
  * **A:** We use SHA-256 as a **Key Derivation Function (KDF)**. The raw Diffie-Hellman shared secret is a mathematical point on an elliptic curve, meaning it is statistically biased and can even vary in length. ChaCha20 requires a perfectly random, uniformly distributed, exact 256-bit key. SHA-256 acts as a cryptographic "blender", compressing the biased mathematical coordinate into a perfectly uniform 256-bit byte array that is safe to use. Additionally, if the ChaCha20 key were to ever leak, SHA-256 acts as a one-way barrier so the attacker cannot reverse-engineer the original ECDH shared secret.

* **Q: What is a Nonce and why is it needed?**
  * **A:** Nonce stands for "Number used once". Stream ciphers like ChaCha20 cannot reuse the same Keystream twice, or else an attacker can XOR two ciphertexts together to reveal the plaintext (Two-Time Pad attack). Generating a new random nonce for every packet ensures the Keystream is totally unique every single time.

* **Q: Edge Case: If the message length exceeds the length of the keystream, does ChaCha20 wrap around?**
  * **A:** **No, it strictly does not wrap around.** ChaCha20 generates its keystream in 64-byte blocks using the Session Key, the Nonce, and an internal 32-bit Block Counter. The maximum value of a 32-bit counter is 4,294,967,295. This means the cipher can generate a maximum of exactly **256 Gigabytes** of keystream for a single message/nonce (4.29 billion blocks × 64 bytes). If a message exceeds 256 GB, the cipher mathematically forbids the counter from wrapping back to 0 (which would cause catastrophic keystream reuse). Instead, the Java Cryptography Extension (JCE) will instantly throw a `ShortBufferException` and abort the encryption. Since our vehicular emergency packets are only a few hundred bytes, we will never hit this 256 GB mathematical limit, making the stream cipher perfectly safe and extremely fast for our use case.

# SecureTextAES

![Text Encyption](https://github.com/user-attachments/assets/e3376686-d2d3-4485-8bc2-0bbe755fbedc)


SecureTextAES is a Java-based project that provides a robust encryption and decryption system using the Advanced Encryption Standard (AES) with Galois/Counter Mode (GCM). This implementation includes additional security layers such as Password-Based Key Derivation (PBKDF2), support for associated authenticated data (AAD), and secure key management for sensitive data.

---

## Features

- **AES-GCM Encryption/Decryption:** Ensures data confidentiality and integrity.
- **Password-Based Key Derivation (PBKDF2):** Securely generates encryption keys using a passphrase and a unique salt.
- **Additional Authenticated Data (AAD):** Enhances security by validating additional contextual data during decryption.
- **Master Key Management:** Implements a master key to securely encrypt document-specific keys.
- **Key Storage:** Manages document-specific keys securely in an external file (`keys.txt`).
- **File Encryption/Decryption:** Supports secure processing of text files.
- **CLI Interface:** Allows users to encrypt and decrypt files interactively via a command-line interface.

---

## How It Works

1. **Master Key Initialization:**
   - The `master.key` file stores a securely generated AES-256 master key.
   - If the file does not exist, the application generates and saves the master key during initialization.

2. **Document-Specific Key Management:**
   - When encrypting a file, a unique key is derived using PBKDF2.
   - The document-specific key is encrypted with the master key and saved in `keys.txt`.

3. **Encryption:**
   - A 96-bit Initialization Vector (IV) is generated for AES-GCM encryption.
   - The plaintext file content is encrypted with the derived key and IV.
   - The AAD ensures the integrity of the additional contextual information.

4. **Decryption:**
   - The application retrieves the document-specific key using the master key.
   - The key and AAD are used to decrypt the file content securely.

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 8 or higher.
- A basic understanding of cryptography and Java programming.

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/ishan22399/SecureTextAES.git
   cd SecureTextAES
   ```

2. Compile the Java files:

   ```bash
   javac *.java
   ```

3. Run the application:

   ```bash
   java AdvancedAES
   ```

---

## Usage

1. **Run the Application:**

   ```bash
   java AdvancedAES
   ```

2. **Follow the CLI Prompts:**
   - Choose to encrypt or decrypt a file.
   - Provide a document ID and the file path.
   - The application handles encryption/decryption and updates the file content.

3. **Key Management:**
   - The master key is stored in `master.key`.
   - Document-specific keys are stored securely in `keys.txt`.

---

## File Structure

- `AdvancedAES.java`: Main application logic for encryption and decryption.
- `KeyManager.java`: Handles key storage, retrieval, and management.
- `CryptoUtils.java`: Provides utility methods for cryptographic operations.
- `master.key`: Stores the AES-256 master key.
- `keys.txt`: Stores encrypted document-specific keys.

---

## Security Features

- **GCM Mode:** Provides built-in data integrity checks.
- **Unique IVs:** Ensures encryption uniqueness for every operation.
- **Salted Key Derivation:** Prevents key reuse across different files.
- **AAD Validation:** Adds an extra layer of data authentication.

---

## Example

### Encrypt a File
1. Run the application and select the encryption option.
2. Provide a unique document ID (e.g., `doc123`) and the file path.
3. The file is encrypted, and its specific key is stored securely.

### Decrypt a File
1. Run the application and select the decryption option.
2. Provide the document ID used during encryption and the file path.
3. The file is decrypted and restored to its original content.

---

## Future Enhancements

- Add support for different encryption algorithms.
- Implement a graphical user interface (GUI).
- Provide secure remote storage for keys.

---

## Happy encrypting! 🔒

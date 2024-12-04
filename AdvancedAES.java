import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Scanner;
import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;
import javax.crypto.spec.PBEKeySpec;

public class AdvancedAES {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int TAG_SIZE = 128; // 128 bits (16 bytes)
    private static final int IV_SIZE = 96; // 96 bits (12 bytes)
    private static final int SALT_LENGTH = 16; // in bytes

    public static SecretKey deriveKey(char[] passphrase, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(passphrase, salt, 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    public static byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    public static String encryptWithAAD(SecretKey key, String plaintext, byte[] additionalData) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_SIZE / 8];
        random.nextBytes(iv);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);

        cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
        cipher.updateAAD(additionalData);
        byte[] plaintextBytes = plaintext.getBytes("UTF-8");
        byte[] ciphertextBytes = cipher.doFinal(plaintextBytes);

        byte[] encrypted = new byte[iv.length + ciphertextBytes.length];
        System.arraycopy(iv, 0, encrypted, 0, iv.length);
        System.arraycopy(ciphertextBytes, 0, encrypted, iv.length, ciphertextBytes.length);

        return Base64.getEncoder().encodeToString(encrypted);
    }

    public static String decryptWithAAD(SecretKey key, String ciphertext, byte[] additionalData) throws Exception {
        byte[] encrypted = Base64.getDecoder().decode(ciphertext);
        if (encrypted.length < IV_SIZE / 8) {
            throw new IllegalArgumentException("Ciphertext is too short to extract IV");
        }

        byte[] iv = new byte[IV_SIZE / 8];
        System.arraycopy(encrypted, 0, iv, 0, iv.length);
        byte[] ciphertextBytes = new byte[encrypted.length - iv.length];
        System.arraycopy(encrypted, iv.length, ciphertextBytes, 0, ciphertextBytes.length);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_SIZE, iv);

        cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);
        cipher.updateAAD(additionalData);

        byte[] plaintextBytes;
        try {
            plaintextBytes = cipher.doFinal(ciphertextBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid ciphertext or key", e);
        }

        return new String(plaintextBytes, "UTF-8");
    }

    public static void processFile(File file, String documentId, byte[] additionalData, boolean encrypt) throws Exception {
        SecretKey masterKey = KeyManager.loadMasterKey();
        SecretKey key;
        if (encrypt) {
            // Generate a new key for this document
            char[] passphrase = "your-secure-passphrase".toCharArray();
            byte[] salt = generateSalt();
            key = deriveKey(passphrase, salt);
            KeyManager.storeKey(documentId, key, masterKey);
        } else {
            // Retrieve the key for this document
            key = KeyManager.retrieveKey(documentId, masterKey);
            if (key == null) {
                throw new IllegalArgumentException("No key found for document ID: " + documentId);
            }
        }

        byte[] fileContent;
        try (FileInputStream fis = new FileInputStream(file);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            fileContent = fis.readAllBytes();
        }

        String result;
        if (encrypt) {
            result = encryptWithAAD(key, new String(fileContent, "UTF-8"), additionalData);
        } else {
            result = decryptWithAAD(key, new String(fileContent, "UTF-8"), additionalData);
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(result.getBytes("UTF-8"));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            // Generate and save master key if it does not exist
            File masterKeyFile = new File("master.key");
            if (!masterKeyFile.exists()) {
                SecretKey masterKey = CryptoUtils.generateMasterKey();
                KeyManager.saveMasterKey(masterKey);
                System.out.println("Master key generated and saved.");
            }

            System.out.println("Choose an option:");
            System.out.println("1. Encrypt a file");
            System.out.println("2. Decrypt a file");
            System.out.print("Enter your choice (1/2): ");
            int choice = scanner.nextInt();
            scanner.nextLine();  // Consume newline

            System.out.print("Enter the document ID: ");
            String documentId = scanner.nextLine();

            System.out.print("Enter the file path: ");
            String filePath = scanner.nextLine();
            File file = new File(filePath);

            byte[] additionalData = "Additional Data".getBytes("UTF-8");

            if (choice == 1) {
                processFile(file, documentId, additionalData, true);
                System.out.println("Document encrypted successfully.");
            } else if (choice == 2) {
                processFile(file, documentId, additionalData, false);
                System.out.println("Document decrypted successfully.");
            } else {
                System.out.println("Invalid choice.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}

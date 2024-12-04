import java.io.*;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;



public class KeyManager {

    private static final String KEY_STORAGE_FILE = "keys.txt";
    private static final String MASTER_KEY_FILE = "master.key";

    public static void storeKey(String documentId, SecretKey key, SecretKey masterKey) throws Exception {
        String encryptedKey = CryptoUtils.encrypt(Base64.getEncoder().encodeToString(key.getEncoded()), masterKey);
        try (FileWriter fw = new FileWriter(KEY_STORAGE_FILE, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(documentId + ":" + encryptedKey);
            bw.newLine();
        }
    }

    public static SecretKey retrieveKey(String documentId, SecretKey masterKey) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(KEY_STORAGE_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts[0].equals(documentId)) {
                    String encryptedKey = parts[1];
                    String keyStr = CryptoUtils.decrypt(encryptedKey, masterKey);
                    byte[] keyBytes = Base64.getDecoder().decode(keyStr);
                    return new SecretKeySpec(keyBytes, "AES");
                }
            }
        }
        return null; // Key not found
    }

    public static SecretKey loadMasterKey() throws Exception {
        try (FileInputStream fis = new FileInputStream(MASTER_KEY_FILE)) {
            byte[] keyBytes = fis.readAllBytes();
            return CryptoUtils.getSecretKeyFromString(new String(keyBytes));
        }
    }

    public static void saveMasterKey(SecretKey masterKey) throws Exception {
        try (FileOutputStream fos = new FileOutputStream(MASTER_KEY_FILE)) {
            fos.write(Base64.getEncoder().encode(masterKey.getEncoded()));
        }
    }
}

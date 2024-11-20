package com.cosek.edms.helper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final String KEY_FILE_PATH = "./secrets/keyfile.key"; // Example path, adjust as necessary

    private static final SecretKey secretKey;

    static {
        try {
            Path keyFilePath = Paths.get(KEY_FILE_PATH);
            Path keyDirectoryPath = keyFilePath.getParent();

            // Create directory if it does not exist
            if (keyDirectoryPath != null && !Files.exists(keyDirectoryPath)) {
                Files.createDirectories(keyDirectoryPath);
            }

            // Load or generate the secret key
            if (Files.exists(keyFilePath)) {
                byte[] keyBytes = Files.readAllBytes(keyFilePath);
                secretKey = getSecretKey(keyBytes);
            } else {
                secretKey = generateOrLoadSecretKey(); // Generate new key if not exist
                Files.write(keyFilePath, secretKey.getEncoded()); // Save the key
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error initializing EncryptionUtil", e);
        }
    }

    public static SecretKey generateOrLoadSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    public static SecretKey getSecretKey(byte[] key) {
        return new SecretKeySpec(key, ALGORITHM);
    }

    public static void encrypt(InputStream input, OutputStream output) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        processStream(input, output, cipher);
    }

    public static void decrypt(InputStream input, OutputStream output) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        processStream(input, output, cipher);
    }

    private static void processStream(InputStream input, OutputStream output, Cipher cipher) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
            if (outputBytes != null) {
                output.write(outputBytes);
            }
        }
        byte[] outputBytes = cipher.doFinal();
        if (outputBytes != null) {
            output.write(outputBytes);
        }
    }
}

package com.edms.file_management.helper;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class EncryptionUtil {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding"; // More secure mode
    private static final int KEY_SIZE = 256;
    private static final String KEY_FILE_PATH = "./secrets/keyfile.key";

    private static final SecretKey secretKey;
    private static final IvParameterSpec iv;

    static {
        try {
            Path keyFilePath = Paths.get(KEY_FILE_PATH);
            Path keyDirectoryPath = keyFilePath.getParent();

            // Ensure directory exists
            if (keyDirectoryPath != null && !Files.exists(keyDirectoryPath)) {
                Files.createDirectories(keyDirectoryPath);
            }

            // Enhanced key and IV generation/loading
            if (Files.exists(keyFilePath)) {
                byte[] fileContent = Files.readAllBytes(keyFilePath);
                // Assume first 32 bytes are key, next 16 are IV
                byte[] keyBytes = Arrays.copyOfRange(fileContent, 0, 32);
                byte[] ivBytes = Arrays.copyOfRange(fileContent, 32, 48);
                secretKey = new SecretKeySpec(keyBytes, "AES");
                iv = new IvParameterSpec(ivBytes);
            } else {
                // Generate new key and IV
                secretKey = generateSecretKey();
                iv = generateIV();

                // Combine key and IV for storage
                byte[] combinedKeyAndIv = ByteBuffer.allocate(secretKey.getEncoded().length + iv.getIV().length)
                        .put(secretKey.getEncoded())
                        .put(iv.getIV())
                        .array();

                Files.write(keyFilePath, combinedKeyAndIv,
                        StandardOpenOption.CREATE,
                        StandardOpenOption.WRITE,
                        StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (Exception e) {
            throw new RuntimeException("Encryption initialization failed", e);
        }
    }

    private static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    private static IvParameterSpec generateIV() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    public static void encrypt(InputStream input, OutputStream output) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

        // Write IV to output first
        output.write(iv.getIV());

        try (CipherOutputStream cos = new CipherOutputStream(output, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                cos.write(buffer, 0, bytesRead);
            }
        }
    }

    public static void decrypt(InputStream input, OutputStream output) throws Exception {
        // Read IV from input
        byte[] ivBytes = new byte[16];
        input.read(ivBytes);
        IvParameterSpec receivedIv = new IvParameterSpec(ivBytes);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, receivedIv);

        try (CipherInputStream cis = new CipherInputStream(input, cipher)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = cis.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
    }
}
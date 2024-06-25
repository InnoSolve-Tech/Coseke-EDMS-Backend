package com.cosek.edms.helper;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;

public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    public static SecretKey generateSecretKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    public static SecretKey getSecretKey(byte[] key) {
        return new SecretKeySpec(key, ALGORITHM);
    }

    public static void encrypt(InputStream input, OutputStream output, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
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

    public static void decrypt(InputStream input, OutputStream output, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
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

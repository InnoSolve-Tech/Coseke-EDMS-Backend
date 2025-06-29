package com.edms.file_management.helper;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Component
public class EncryptionUtil {
    @Value("${sftp.username}")
    private String sftpUsername;

    @Value("${sftp.password}")
    private String sftpPassword;

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort = 22;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final String KEY_FILE_PATH = "/uploads/edms/key";

    public SecretKey generateOrLoadSecretKey() throws Exception {
        System.out.println("Generating new AES secret key with size: " + KEY_SIZE + " bits");
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        SecretKey key = keyGen.generateKey();
        System.out.println("Successfully generated new secret key");
        return key;
    }

    public SecretKey getSecretKey() throws Exception {
        System.out.println("Attempting to retrieve secret key from SFTP: " + KEY_FILE_PATH);
        try {
            byte[] keyBytes = downloadFromSftp();
            if (keyBytes != null && keyBytes.length > 0) {
                System.out.println("Successfully downloaded existing key from SFTP (" + keyBytes.length + " bytes)");
                return new SecretKeySpec(keyBytes, ALGORITHM);
            }
        } catch (Exception e) {
            System.out.println("Failed to download existing key from SFTP: " + e.getMessage());
        }

        System.out.println("Generating new secret key and uploading to SFTP");
        SecretKey secretKey = generateOrLoadSecretKey();
        try {
            uploadFileViaSFTP(secretKey.getEncoded(), sftpUsername, sftpHost, sftpPort, sftpPassword);
            System.out.println("Successfully uploaded new key to SFTP");
        } catch (Exception e) {
            System.out.println("Failed to upload new key to SFTP: " + e.getMessage());
            throw e;
        }
        return secretKey;
    }

    public void encrypt(InputStream input, OutputStream output) throws Exception {
        System.out.println("Starting encryption process");
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKey secretKey = getSecretKey();
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        processStream(input, output, cipher);
        System.out.println("Encryption completed successfully");
    }

    public void decrypt(InputStream input, OutputStream output) throws Exception {
        System.out.println("Starting decryption process");
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKey secretKey = getSecretKey();
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        processStream(input, output, cipher);
        System.out.println("Decryption completed successfully");
    }

    private void processStream(InputStream input, OutputStream output, Cipher cipher) throws Exception {
        byte[] buffer = new byte[1024];
        int bytesRead;
        int totalBytesProcessed = 0;

        while ((bytesRead = input.read(buffer)) != -1) {
            byte[] outputBytes = cipher.update(buffer, 0, bytesRead);
            if (outputBytes != null) {
                output.write(outputBytes);
                totalBytesProcessed += bytesRead;
            }
        }

        byte[] finalBytes = cipher.doFinal();
        if (finalBytes != null) {
            output.write(finalBytes);
        }

        System.out.println("Processed " + totalBytesProcessed + " bytes through cipher");
    }

    private byte[] downloadFromSftp() {
        System.out.println("Connecting to SFTP server: " + sftpHost + ":" + sftpPort);
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);

            Properties config = new Properties();
            config.setProperty("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("SFTP session connected successfully");

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            System.out.println("SFTP channel opened successfully");

            String remoteDir = EncryptionUtil.KEY_FILE_PATH.substring(0, EncryptionUtil.KEY_FILE_PATH.lastIndexOf("/"));
            String remoteFile = EncryptionUtil.KEY_FILE_PATH.substring(EncryptionUtil.KEY_FILE_PATH.lastIndexOf("/") + 1);

            System.out.println("Navigating to remote directory: " + remoteDir);
            channel.cd(remoteDir);

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            System.out.println("Downloading file: " + remoteFile);
            channel.get(remoteFile, output);

            byte[] result = output.toByteArray();
            System.out.println("Successfully downloaded file (" + result.length + " bytes)");
            return result;

        } catch (Exception e) {
            System.out.println("Error during SFTP download: " + e.getMessage());
            return null;
        } finally {
            try {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
                System.out.println("SFTP connections closed");
            } catch (Exception e) {
                System.out.println("Error closing SFTP connections: " + e.getMessage());
            }
        }
    }

    private void uploadFileViaSFTP(byte[] data, String username,
                                   String host, int port, String password) throws Exception {
        System.out.println("Uploading file to SFTP: " + EncryptionUtil.KEY_FILE_PATH + " (" + data.length + " bytes)");
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.setProperty("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            System.out.println("SFTP session connected for upload");

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();
            System.out.println("SFTP channel opened for upload");

            String remoteDir = EncryptionUtil.KEY_FILE_PATH.substring(0, EncryptionUtil.KEY_FILE_PATH.lastIndexOf("/"));
            try {
                System.out.println("Navigating to directory: " + remoteDir);
                channel.cd(remoteDir);
            } catch (Exception e) {
                System.out.println("Directory doesn't exist, creating: " + remoteDir);
                createDirectoryRecursively(channel, remoteDir);
                channel.cd(remoteDir);
            }

            String remoteFilename = EncryptionUtil.KEY_FILE_PATH.substring(EncryptionUtil.KEY_FILE_PATH.lastIndexOf("/") + 1);
            System.out.println("Uploading file: " + remoteFilename);
            channel.put(new ByteArrayInputStream(data), remoteFilename);
            System.out.println("File uploaded successfully");

        } catch (Exception e) {
            System.out.println("Error during SFTP upload: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (channel != null) {
                    channel.disconnect();
                }
                if (session != null) {
                    session.disconnect();
                }
                System.out.println("SFTP upload connections closed");
            } catch (Exception e) {
                System.out.println("Error closing SFTP upload connections: " + e.getMessage());
            }
        }
    }

    private void createDirectoryRecursively(ChannelSftp channel, String path) throws Exception {
        String[] directories = path.split("/");
        StringBuilder currentPath = new StringBuilder();

        for (String directory : directories) {
            if (directory.isEmpty()) {
                currentPath.append("/");
                continue;
            }

            currentPath.append(directory).append("/");
            String pathToCreate = currentPath.toString();

            try {
                channel.cd(pathToCreate);
                System.out.println("Directory exists: " + pathToCreate);
            } catch (Exception e) {
                System.out.println("Creating directory: " + pathToCreate);
                channel.mkdir(pathToCreate);
                channel.cd(pathToCreate);
            }
        }
    }
}
package com.cosek.edms.settings;

import com.cosek.edms.helper.EncryptionUtil;
import com.cosek.edms.user.User;
import com.cosek.edms.user.UserRepository;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;
    private final EncryptionUtil encryptionUtil;

    @Value("${sftp.username}")
    private String sftpUsername;

    @Value("${sftp.password}")
    private String sftpPassword;

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort = 22;

    @Value("${server.port}")
    private String serverPort;

    private final String remoteLogosPath = "/uploads/logos"; // SFTP folder for logos

    public Optional<Settings> getSettings() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromAuth(authentication);
        User user = userRepository.findByEmail(email).orElseThrow();
        return Optional.ofNullable(settingsRepository.findByUser(user))
                .orElseThrow(() -> new RuntimeException("No settings found"));
    }

    @Transactional
    public Settings createOrUpdateSettings(Settings settings, MultipartFile logo) throws Exception {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromAuth(authentication);
        User user = userRepository.findByEmail(email).orElseThrow();

        Optional<Settings> exSettings = settingsRepository.findByUser(user);
        Settings existingSettings;
        Settings settingsToUpdate;
        if (exSettings.isPresent()) {
            existingSettings = exSettings.get();
            existingSettings.setCompanyName(settings.getCompanyName());
            existingSettings.setColors(settings.getColors());
            existingSettings.setCurrency(settings.getCurrency());
            settingsToUpdate = existingSettings;
        } else {
            settings.setUser(user);
            settingsToUpdate = settings;
            existingSettings = settings;
        }

        if (logo != null && !logo.isEmpty()) {
            String fileHash = UUID.randomUUID().toString();
            String extension = getFileExtension(logo.getOriginalFilename());
            String storedFileName = fileHash + extension;

            // Encrypt file in memory
            ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();
            try (var input = logo.getInputStream()) {
                encryptionUtil.encrypt(input, encryptedOutput);
            }

            // Upload encrypted logo to SFTP
            // upload via http://192.168.100.40:8081/file-management/api/v1/files/storage
            uploadFileViaSFTP(
                    encryptedOutput.toByteArray(),
                    remoteLogosPath + "/" + storedFileName,
                    sftpUsername,
                    sftpHost,
                    sftpPort,
                    sftpPassword
            );

            // Update logo URL to the relative path for retrieval
            settingsToUpdate.setLogoUrl("/" + storedFileName);

            // Optionally delete old logo from SFTP
            if (existingSettings != null && existingSettings.getLogoUrl() != null) {
                try {
                    deleteFileFromSftp(remoteLogosPath + existingSettings.getLogoUrl());
                } catch (Exception e) {
                    System.out.println("Failed to delete old logo from SFTP: " + e.getMessage());
                }
            }
        }

        return settingsRepository.save(settingsToUpdate);
    }

    /**
     * Upload encrypted file to SFTP server
     */
    private void uploadFileViaSFTP(
            byte[] encryptedData,
            String remoteFilePath,
            String username,
            String host,
            int port,
            String password
    ) throws Exception {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            String remoteDir = remoteFilePath.substring(0, remoteFilePath.lastIndexOf("/"));
            try {
                channel.cd(remoteDir);
            } catch (Exception e) {
                channel.cd(remoteDir);
            }

            String remoteFilename = remoteFilePath.substring(remoteFilePath.lastIndexOf("/") + 1);
            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(encryptedData)) {
                channel.put(inputStream, remoteFilename);
            }

        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    /**
     * Delete file from SFTP server
     */
    private boolean deleteFileFromSftp(String path) throws Exception {
        String remoteFilePath = remoteLogosPath + "/" + path;

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channel = null;

        try {
            session = jsch.getSession(sftpUsername, sftpHost, sftpPort);
            session.setPassword(sftpPassword);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            channel = (ChannelSftp) session.openChannel("sftp");
            channel.connect();

            channel.rm(remoteFilePath);

        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
        return true;
    }


    public String getFileLinkByHash(String hash) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = getEmailFromAuth(authentication);
        User user = userRepository.findByEmail(email).orElseThrow();
        Optional<Settings> file = settingsRepository.findByUser(user);

        // Return relative API route for fetching the file
        if (file.isPresent()) {
            return "/settings" + file.get().getLogoUrl();
        } else {
            throw new RuntimeException("File with hash " + hash + " not found");
        }

    }

    private String getEmailFromAuth(org.springframework.security.core.Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex == -1 ? "" : filename.substring(dotIndex);
    }
}

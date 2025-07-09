package com.edms.file_management.fileVersions;

import static com.edms.file_management.helper.Constants.REMOTE_FILE_PATH;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.edms.file_management.filemanager.FileManager;
import com.edms.file_management.filemanager.FileManagerRepository;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileVersionsService {
    @Value("${sftp.username:}")
    private String sftpUsername;

    @Value("${sftp.password:}")
    private String sftpPassword;

    @Value("${sftp.host:}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Value("${sftp.max-files:20}")
    private int MAX_FILES_PER_FOLDER;

    private final FileVersionsRepository fileVersionsRepository;
    private final FileManagerRepository fileManagerRepository;
    private final ConcurrentHashMap<String, AtomicInteger> folderFileCountCache;

    @Autowired
    public FileVersionsService (FileManagerRepository fileManagerRepository,FileVersionsRepository fileVersionsRepository,ConcurrentHashMap<String, AtomicInteger> folderFileCountCache) {
        this.folderFileCountCache = folderFileCountCache;
        this.fileManagerRepository = fileManagerRepository;
        this.fileVersionsRepository = fileVersionsRepository;
    }

    public void createFileVersion(Long fileId, String versionName) {
        FileManager fileManager = fileManagerRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("FileManager with ID " + fileId + " not found"));
        String filepath;
        try {
            filepath = generateFilePath(fileManager.getFilename(), fileManager.getHashName(), versionName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        FileVersions fileVersions = FileVersions.builder()
                .versionName(versionName)
                .fileManager(fileManager)
                .filePath(filepath)
                .build();
        fileVersionsRepository.save(fileVersions);
    }

    /**
     * Gets the final storage path (local or SFTP base path)
     */
    public String getFinalStoragePath(String relativePath) {
        return REMOTE_FILE_PATH + "/" + relativePath;
    }

    /**
     * Counts files in a folder (works for both local and SFTP)
     */
    private int getFileCountInFolder(String fullPath, String relativePath) throws Exception {
        // Check cache first
        AtomicInteger cachedCount = folderFileCountCache.get(relativePath);
        if (cachedCount != null) {
            return cachedCount.get();
        }
        int count = countFilesInSFTPFolder(fullPath);
        // Update cache
        folderFileCountCache.put(relativePath, new AtomicInteger(count));
        return count;
    }


    /**
     * Counts files in SFTP folder
     */
    private int countFilesInSFTPFolder(String remoteFolderPath) throws Exception {
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

            try {
                @SuppressWarnings("unchecked")
                Vector<ChannelSftp.LsEntry> files = channel.ls(remoteFolderPath);
                // Filter out directories (. and ..)
                return (int) files.stream()
                        .filter(entry -> !entry.getAttrs().isDir())
                        .count();
            } catch (Exception e) {
                // Folder doesn't exist, return 0
                return 0;
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
     * Finds an available folder that has space (less than 20 files)
     * or creates a new one if all existing folders are full
     */
    private int findAvailableFolder(String basePath) throws Exception {
        int folderNumber = 0;

        while (true) {
            String folderPath = basePath + "/folder-" + folderNumber;
            String fullPath = getFinalStoragePath(folderPath);

            int fileCount = getFileCountInFolder(fullPath, folderPath);

            if (fileCount < MAX_FILES_PER_FOLDER) {
                // Update cache
                folderFileCountCache.computeIfAbsent(folderPath, k -> new AtomicInteger(0))
                        .set(fileCount);
                return folderNumber;
            }

            folderNumber++;

            // Safety check to prevent infinite loop (optional)
            if (folderNumber > 1000) {
                throw new Exception("Too many folders created. Please check the system.");
            }
        }
    }


    public String generateFilePath(String originalFilename, String hashName, String versionName) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));
        String day = now.format(DateTimeFormatter.ofPattern("dd"));
        String hour = now.format(DateTimeFormatter.ofPattern("HH"));

        // Base path: Year/Month/Day/Hour
        String basePath = year + "/" + month + "/" + day + "/" + hour;

        // Find the appropriate folder number
        int folderNumber = findAvailableFolder(basePath);

        // Complete path structure
        String folderName = "folder-" + folderNumber;
        String completePath = basePath + "/" + folderName + "/" + hashName;

        // Return the complete file path
        return completePath + "/" + versionName + ".bin";
    }


}

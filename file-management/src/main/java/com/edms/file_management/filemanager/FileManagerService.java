package com.edms.file_management.filemanager;

import static com.edms.file_management.helper.Constants.REMOTE_FILE_PATH;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.edms.file_management.config.StorageProperties;
import com.edms.file_management.directoryAccessControl.DirectoryAccessControl;
import com.edms.file_management.fileAccessControl.FileAccessControl;
import com.edms.file_management.fileAccessControl.FileAccessControlRepository;
import com.edms.file_management.fileVersions.FileVersionsRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.edms.file_management.directory.Directory;
import com.edms.file_management.directory.DirectoryRepository;
import com.edms.file_management.directory.DirectoryService;
import com.edms.file_management.exception.ResourceNotFoundException;
import com.edms.file_management.fileVersions.FileVersions;
import com.edms.file_management.fileVersions.FileVersionsService;
import com.edms.file_management.helper.EncryptionUtil;
import com.edms.file_management.helper.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@AllArgsConstructor
public class FileManagerService implements StorageService {

    private final String rootLocation;

    @Value("${sftp.username:}")
    private String sftpUsername;

    @Value("${sftp.password:}")
    private String sftpPassword;

    @Value("${sftp.host:}")
    private String sftpHost;

    @Value("${sftp.port:22}")
    private int sftpPort;

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private FileManagerRepository fileManagerRepository;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileManagerRepository fileRepository;

    @Autowired
    private FileVersionsService fileVersionsService;

    @Autowired
    private FileVersionsRepository fileVersionsRepository;

    @Autowired
    private FileAccessControlRepository accessControlRepository;

    @Autowired
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, AtomicInteger> folderFileCountCache;


    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public FileManagerService(
            StorageProperties properties,
            ConcurrentHashMap<String, AtomicInteger> folderFileCountCache
    ) throws Exception {
        if(properties.getLocation().trim().length() == 0){
            throw new Exception("File upload location can not be Empty.");
        }
        this.rootLocation = properties.getLocation();
        this.folderFileCountCache = folderFileCountCache;
        this.objectMapper = new ObjectMapper();
    }




    /**
     * Creates SFTP directory structure
     */
    private void createSFTPDirectoryStructure(String remotePath) throws Exception {
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

            String dirPath = remotePath.substring(0, remotePath.lastIndexOf("/"));
            createRemoteDirectory(channel, dirPath);

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
     * Store file via SFTP with structured path
     */
    private void storeFileViaSFTPWithPath(MultipartFile file, String filePath) throws Exception {
        // Encrypt file in memory
        ByteArrayOutputStream encryptedOutput = new ByteArrayOutputStream();
        try (InputStream input = file.getInputStream()) {
            encryptionUtil.encrypt(input, encryptedOutput);
        }

        // Create directory structure first
        createSFTPDirectoryStructure(REMOTE_FILE_PATH + "/" + filePath);

        // SFTP upload
        String remotePath = REMOTE_FILE_PATH + "/" + filePath;
        uploadFileViaSFTP(
                encryptedOutput.toByteArray(),
                remotePath,
                sftpUsername,
                sftpHost,
                sftpPort,
                sftpPassword
        );
    }

    /**
     * Updates folder cache after file addition
     */
    private void updateFolderCache(String filePath) {
        // Extract folder path from file path
        String[] pathParts = filePath.split("/");
        if (pathParts.length >= 3) {
            String year = pathParts[0];
            String month = pathParts[1];
            String folderName = pathParts[2];
            String folderPath = year + "/" + month + "/" + folderName;

            AtomicInteger count = folderFileCountCache.get(folderPath);
            if (count != null) {
                count.incrementAndGet();
            }
        }
    }

    @Transactional
    public FileAccessControl updateFileAccessControl(Long id,FileAccessControl accessControl) throws Exception{
        FileManager file = fileManagerRepository.findById(id).orElseThrow(() -> new RuntimeException("Failed to find file!"));
            if(accessControl.getAccessType() != null) {
                FileAccessControl savedAccessControl = accessControlRepository.save(accessControl);
                file.setAccessControl(savedAccessControl);
                return savedAccessControl;
            } else {
                throw new RuntimeException("Access type should not be null!");
            }
    }


    @Override
    @Transactional
    public List<FileManager> bulkStore(FileManager[] data, MultipartFile[] files) throws Exception {
        List<FileManager> createdFiles = new ArrayList<>();

        if (data.length != files.length) {
            throw new Exception("Mismatch between file count and metadata count.");
        }

        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                FileManager fileData = data[i];

                if (file.isEmpty()) {
                    throw new Exception("Failed to store empty file: " + file.getOriginalFilename());
                }

                // Find or create directory
                Optional<Directory> directory = directoryRepository.findByName(fileData.getDocumentType());
                Directory targetDirectory;

                if (directory.isPresent()) {
                    targetDirectory = directory.get();
                } else {
                    Directory newDirectory = Directory.builder()
                            .name(fileData.getDocumentType())
                            .build();
                    targetDirectory = directoryService.creaDirectoryWithName(newDirectory);
                }
                LocalDateTime now = LocalDateTime.now();
                // Generate hash after saving (so we have createdDate)
                String hash = HashUtil.generateHash(data[i].getFilename(), now);

                // Generate structured file path
                String filePath = fileVersionsService.generateFilePath(data[i].getFilename(), hash, "v1.0");

                // Create FileManager entity
                FileManager fileManager = FileManager.builder()
                        .documentType(fileData.getDocumentType())
                        .folderID(targetDirectory.getFolderID())
                        .hashName(hash)
                        .filename(file.getOriginalFilename())
                        .documentName(fileData.getDocumentName())
                        .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .metadata(fileData.getMetadata())
                        .build();

                // Save to get ID and timestamp
                fileManager = fileRepository.save(fileManager);

                fileVersionsService.createFileVersion(fileManager.getId(), "v1.0");

                storeFileViaSFTPWithPath(file, filePath);

                // Update cache after successful file storage
                updateFolderCache(filePath);

                // Save again with hash
                fileManager = fileRepository.save(fileManager);
                createdFiles.add(fileManager);
            }
        } catch (Exception e) {
            throw new Exception("Failed to store files: " + e.getMessage(), e);
        }

        return createdFiles;
    }

    @Override
    @Transactional
    public FileManager store(FileManager data, MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Failed to store empty file.");
        }

        // Find or create directory
        Optional<Directory> directory = directoryRepository.findByName(data.getDocumentType());
        Directory targetDirectory;
        LocalDateTime now = LocalDateTime.now();
        // Generate hash after saving (so we have createdDate)
        String hash = HashUtil.generateHash(data.getFilename(), now);
        String filePath = fileVersionsService.generateFilePath(data.getFilename(), hash, "v1.0");
        storeFileViaSFTPWithPath(file, filePath);

        if (directory.isPresent()) {
            targetDirectory = directory.get();
        } else {
            Directory newDirectory = Directory.builder()
                    .name(data.getDocumentType())
                    .build();
            targetDirectory = directoryService.creaDirectoryWithName(newDirectory);
            targetDirectory.setParentFolderID((int) targetDirectory.getFolderID());
            directoryRepository.save(targetDirectory);
        }

        // Create FileManager entity
        FileManager fileManager = FileManager.builder()
                .documentType(data.getDocumentType())
                .folderID(targetDirectory.getFolderID())
                .filename(file.getOriginalFilename())
                .hashName(hash)
                .documentName(data.getDocumentName())
                .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .metadata(data.getMetadata())
                .build();

        // Save to get ID and timestamp
        fileManager = fileRepository.save(fileManager);

        fileVersionsService.createFileVersion(fileManager.getId(), "v1.0");


        // Update cache after successful file storage
        updateFolderCache(filePath);

        // Save again with hash
        return fileRepository.save(fileManager);
    }

    public void deleteFileByHashName(String hashName) throws Exception {
        FileManager file = fileRepository.findByHashName(hashName)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with hash: " + hashName));

        // Delete all versions of the file from SFTP server
        deleteAllFileVersionsFromSFTP(file);

        // Delete from database (cascade will handle file versions)
        fileRepository.delete(file);
    }

    private void deleteAllFileVersionsFromSFTP(FileManager file) throws Exception {
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

            // Delete each version of the file
            for (FileVersions version : file.getFileVersions()) {
                if (version.getFilePath() != null && !version.getFilePath().isEmpty()) {
                    String remoteFilePath = REMOTE_FILE_PATH + "/" + version.getFilePath();
                    try {
                        channel.rm(remoteFilePath);
                        System.out.println("Deleted file version: " + remoteFilePath);
                    } catch (Exception e) {
                        System.err.println("Failed to delete file version: " + remoteFilePath + " - " + e.getMessage());
                        // Continue with other versions even if one fails
                    }
                }
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


    public FileManager updateFile(FileVersions data, MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Failed to store empty file.");
        }
        FileManager fileManager = fileManagerRepository.findById(data.getFileManager().getId()).orElseThrow(() -> new RuntimeException("File Manager not found!!"));
        fileVersionsService.createFileVersion(fileManager.getId(), data.getVersionName());
        String filePath = fileVersionsService.generateFilePath(data.getFileManager().getFilename(), data.getFileManager().getHashName(), data.getVersionName());
        storeFileViaSFTPWithPath(file, filePath);
        // Update cache after successful file storage
        updateFolderCache(filePath);
        // Save again with hash
        return fileRepository.save(fileManager);
    }

    @Transactional
    public FileManager storeById(FileManager data, MultipartFile file, Long folderId) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Failed to store empty file.");
        }

        // Find directory by ID
        Directory directory = directoryRepository.findById(folderId)
                .orElseThrow(() -> new Exception("Folder with id " + folderId + " doesn't exist!"));
        // Generate hash after saving (so we have createdDate)
        LocalDateTime now = LocalDateTime.now();
        String hash = HashUtil.generateHash(data.getFilename(), now);
        String filePath = fileVersionsService.generateFilePath(data.getFilename(), hash, "v1.0");
        storeFileViaSFTPWithPath(file, filePath);

        // Create FileManager entity
        FileManager fileManager = FileManager.builder()
                .documentType(data.getDocumentType())
                .documentName(data.getDocumentName())
                .folderID(directory.getFolderID())
                .filename(file.getOriginalFilename())
                .hashName(hash)
                .metadata(data.getMetadata())
                .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();

        // Save to get ID and timestamp
        fileManager = fileRepository.save(fileManager);
        fileVersionsService.createFileVersion(fileManager.getId(), "v1.0");

        // Update cache after successful file storage
        updateFolderCache(filePath);

        // Save again with hash
        return fileRepository.save(fileManager);
    }

    @Transactional
    public void bulkStoreById(List<FileManager> fileManagers, MultipartFile[] files, Long folderId) throws Exception {
        if (files.length != fileManagers.size()) {
            throw new Exception("Mismatch between file count and metadata count.");
        }

        // Find directory by ID
        Directory directory = directoryRepository.findById(folderId)
                .orElseThrow(() -> new Exception("Folder with ID " + folderId + " not found."));

        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                FileManager fileManager = fileManagers.get(i);

                if (file.isEmpty()) {
                    throw new Exception("Failed to store empty file: " + file.getOriginalFilename());
                }
                LocalDateTime now = LocalDateTime.now();
                // Generate hash after saving (so we have createdDate)
                String hash = HashUtil.generateHash(fileManager.getFilename(), now);
                fileManager.setHashName(hash);

                // Generate structured file path
                String filePath = fileVersionsService.generateFilePath(fileManager.getFilename(), hash, "v1.0");
                storeFileViaSFTPWithPath(file, filePath);

                // Update the FileManager with file-specific information
                fileManager.setFilename(file.getOriginalFilename());
                fileManager.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
                fileManager.setFolderID(directory.getFolderID());

                // Save to get ID and timestamp
                fileManager = fileRepository.save(fileManager);

                fileVersionsService.createFileVersion(fileManager.getId(), "v1.0");

                // Update cache after successful file storage
                updateFolderCache(filePath);

                // Save again with hash
                fileRepository.save(fileManager);
            }
        } catch (Exception e) {
            throw new Exception("Failed to store files: " + e.getMessage(), e);
        }
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
                // Directory doesn't exist, create it
                createRemoteDirectory(channel, remoteDir);
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
     * Create remote directory recursively
     */
    private void createRemoteDirectory(ChannelSftp channel, String remotePath) throws Exception {
        String[] directories = remotePath.split("/");
        String currentPath = "";

        for (String dir : directories) {
            if (dir.isEmpty()) continue;

            currentPath += "/" + dir;
            try {
                channel.cd(currentPath);
            } catch (Exception e) {
                channel.mkdir(currentPath);
                channel.cd(currentPath);
            }
        }
    }

    public Path getEncryptedFilePath(String hash, String version) throws Exception {
        FileManager file = fileRepository.findByHashName(hash)
                .orElseThrow(() -> new Exception("File with hash " + hash + " not found"));

        FileVersions latestVersion;

        if (version != null) {
            latestVersion = fileVersionsRepository.findFirstByVersionNameAndFileManager_Id(version, file.getId())
                    .orElseThrow(() -> new RuntimeException("File with this version doesn't exist!"));
        } else {
            latestVersion = getLastFileVersion(file.getFileVersions());
            if (latestVersion == null) {
                throw new Exception("No valid file versions found for file with hash " + hash);
            }
        }

        // For SFTP, download the file to a temporary location
        return downloadFileFromSFTPByPath(latestVersion.getFilePath());
    }

    private FileVersions getLastFileVersion(List<FileVersions> fileVersions) {
        FileVersions latestVersion = null;
        double maxVersion = -1.0; // Initialize to -1 to handle version 0.0

        for (FileVersions version : fileVersions) {
            String versionString = version.getVersionName();
            if (versionString != null && versionString.startsWith("v")) {
                try {
                    // Remove 'v' prefix and parse as double
                    double currentVersion = Double.parseDouble(versionString.substring(1));
                    if (currentVersion > maxVersion) {
                        maxVersion = currentVersion;
                        latestVersion = version;
                    }
                } catch (NumberFormatException e) {
                    // Log the invalid version format if needed
                    System.err.println("Invalid version format: " + versionString);
                    // Skip invalid version formats
                    continue;
                }
            }
        }

        return latestVersion;
    }
    /**
     * Download file from SFTP server using the stored file path
     */
    private Path downloadFileFromSFTPByPath(String filePath) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            throw new Exception("File path is null or empty");
        }

        String remoteFilePath = REMOTE_FILE_PATH + "/" + filePath;
        String fileExtension = filePath.substring(filePath.lastIndexOf('.'));
        Path tempFile = Files.createTempFile("sftp_download_", fileExtension);

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

            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                channel.get(remoteFilePath, outputStream);
            }

            return tempFile;

        } catch (Exception e) {
            // Clean up temp file if download failed
            Files.deleteIfExists(tempFile);
            throw new Exception("Failed to download file from SFTP: " + remoteFilePath, e);
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex);
    }
    // ... (rest of the existing methods remain unchanged)

    @Override
    public Stream<Path> loadAll(Long folderID) throws Exception {
        try {
            String folderPath = directoryService.getDirectoryPath(folderID);
            Path destinationPath = Paths.get(folderPath);
            return Files.walk(destinationPath, 1)
                .filter(path -> !path.equals(destinationPath))
                .map(destinationPath::relativize);
        }
        catch (IOException e) {
            throw new Exception("Failed to read stored files", e);
        }
    }

    public List<FileManager> getAllFiles(Long folderId) {
        return fileRepository.findByFolderID(folderId).orElseThrow();
    }

    @Override
    public Path load(String filename, Long folderID) {
        String folderPath = directoryService.getDirectoryPath(folderID);
        Path destinationPath = Paths.get(folderPath);
        return destinationPath.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename, Long folderID) throws Exception {
        try {
            Path file = load(filename, folderID);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new Exception("Could not read file: " + filename);
            }
        }
        catch (MalformedURLException e) {
            throw new Exception("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        try {
            Path root = Paths.get(rootLocation);
            FileSystemUtils.deleteRecursively(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void decryptFile(Path encryptedFilePath, OutputStream outputStream) throws Exception {
        try (InputStream inputStream = Files.newInputStream(encryptedFilePath)) {
            if (inputStream == null) {
                throw new IOException("Unable to create input stream for encrypted file");
            }
            encryptionUtil.decrypt(inputStream, outputStream);
        } catch (Exception e) {
            throw new Exception("Failed to decrypt file: " + e.getMessage(), e);
        }
    }

    public List<FileManager> getFilesInStore() {
        return fileRepository.findAll();
    }

    public FileManager getFileByID(Long id) {
        return fileRepository.findById(id).orElseThrow();
    }

    @Transactional
    public FileManager updateFile(FileManager fileManager) {
        FileManager file = fileRepository.findByHashName(fileManager.getHashName()).orElseThrow();
        file.setMetadata(fileManager.getMetadata());
        if(fileManager.getDocumentType() != null) {
            file.setDocumentType(fileManager.getDocumentType());
        }
        file.setDocumentName(fileManager.getDocumentName());
        fileRepository.save(file);
        return file;
    }

    public List<FileManager> searchFiles(String keyword) {
        Optional<List<FileManager>> optionalFiles = fileRepository.searchFiles(keyword);
        return optionalFiles.orElseThrow(() -> new ResourceNotFoundException("No files found with keyword: " + keyword));
    }

    public FileManager convertStringToDataManager(String fileData) {
        try {
            return objectMapper.readValue(fileData, FileManager.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON string to FileManager: " + e.getMessage(), e);
        }
    }

    public void deleteFileById(Long id) throws Exception {
        FileManager file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));
            // Delete from SFTP server
        deleteFileFromSFTP(file.getHashName(), getFileExtension(file.getFilename()));


        fileRepository.delete(file);
    }

    /**
     * Delete file from SFTP server
     */
    private void deleteFileFromSFTP(String hash, String extension) throws Exception {
        String remoteFilePath = REMOTE_FILE_PATH + "/" + hash + extension;

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
    }

    @Transactional
    public FileManager updateMetadata(Long fileId, Map<String, Object> newMetadata) {
        FileManager file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        file.getMetadata().putAll(newMetadata);
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public FileManager deleteMetadata(Long fileId, List<String> keys) {
        FileManager file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        for (String key : keys) {
            file.getMetadata().remove(key);
        }
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public FileManager clearMetadata(Long fileId) {
        FileManager file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        file.setMetadata(null);
        fileRepository.save(file);
        return file;
    }

    public List<FileManager> getAllFiles() {
        return fileRepository.findAll();
    }

    public List<FileManager> fullTextSearch(String searchTerm) throws Exception {
        List<FileManager> results = new ArrayList<>();
        List<FileManager> allFiles = getAllFiles();

        for (FileManager file : allFiles) {
            if (file.getHashName() == null || file.getHashName().isEmpty()) {
                continue;
            }

            String mimeType = file.getMimeType();
            String extension = getFileExtension(file.getFilename()).toLowerCase();

            if (!isSearchableDocument(mimeType, extension)) {
                continue;
            }

            try {
                Path encryptedFilePath = getEncryptedFilePath(file.getHashName(), null);

                if (!Files.exists(encryptedFilePath)) {
                    continue;
                }

                Path tempFile = Files.createTempFile("decrypted_", extension);

                try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                    decryptFile(encryptedFilePath, outputStream);
                    String fileContent = extractTextFromFile(tempFile.toFile(), mimeType, extension);

                    if (fileContent != null && fileContent.toLowerCase().contains(searchTerm.toLowerCase())) {
                        results.add(file);
                    }
                } finally {
                    Files.deleteIfExists(encryptedFilePath);
                }
            } catch (Exception e) {
                System.err.println("Error searching in file " + file.getFilename() + ": " + e.getMessage());
            }
        }

        return results;
    }

    private boolean isSearchableDocument(String mimeType, String extension) {
        if (mimeType == null) {
            mimeType = "";
        }

        return mimeType.contains("application/vnd.openxmlformats-officedocument.wordprocessingml.document") ||
                mimeType.contains("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") ||
                mimeType.contains("application/vnd.ms-excel") ||
                mimeType.contains("application/pdf") ||
                extension.equals(".docx") ||
                extension.equals(".xlsx") ||
                extension.equals(".xls") ||
                extension.equals(".pdf");
    }

    private String extractTextFromFile(File file, String mimeType, String extension) throws Exception {
        if (extension.equals(".pdf")) {
            return extractTextFromPdf(file);
        } else if (extension.equals(".docx")) {
            return extractTextFromDocx(file);
        } else if (extension.equals(".xlsx") || extension.equals(".xls")) {
            return extractTextFromExcel(file);
        } else {
            if (mimeType.contains("pdf")) {
                return extractTextFromPdf(file);
            } else if (mimeType.contains("word")) {
                return extractTextFromDocx(file);
            } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
                return extractTextFromExcel(file);
            }
        }

        return null;
    }

    private String extractTextFromPdf(File file) throws Exception {
        try (PDDocument document = PDDocument.load(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractTextFromDocx(File file) throws Exception {
        try (XWPFDocument document = new XWPFDocument(Files.newInputStream(file.toPath()))) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }

    private String extractTextFromExcel(File file) throws Exception {
        StringBuilder textContent = new StringBuilder();

        try (Workbook workbook = WorkbookFactory.create(file)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                textContent.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    for (Cell cell : row) {
                        switch (cell.getCellType()) {
                            case STRING:
                                textContent.append(cell.getStringCellValue()).append(" ");
                                break;
                            case NUMERIC:
                                textContent.append(cell.getNumericCellValue()).append(" ");
                                break;
                            case BOOLEAN:
                                textContent.append(cell.getBooleanCellValue()).append(" ");
                                break;
                            case FORMULA:
                                textContent.append(cell.getCellFormula()).append(" ");
                                break;
                            default:
                                break;
                        }
                    }
                    textContent.append("\n");
                }
            }
        }

        return textContent.toString();
    }
}
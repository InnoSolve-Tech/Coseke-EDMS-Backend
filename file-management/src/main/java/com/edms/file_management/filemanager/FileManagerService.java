package com.edms.file_management.filemanager;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.edms.file_management.config.StorageProperties;
import com.edms.file_management.directory.Directory;
import com.edms.file_management.directory.DirectoryRepository;
import com.edms.file_management.directory.DirectoryService;
import com.edms.file_management.exception.ResourceNotFoundException;
import com.edms.file_management.helper.EncryptionUtil;
import com.edms.file_management.helper.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import jakarta.transaction.Transactional;

@Service
@RequiredArgsConstructor
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

    @Value("${sftp.enabled:false}")
    private boolean sftpEnabled;

    // Base SFTP upload directory
    private final String remoteBasePath = "/uploads";

    @Autowired
    private DirectoryService directoryService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private FileManagerRepository fileRepository;

    @Autowired
    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, AtomicInteger> folderFileCountCache = new ConcurrentHashMap<>();

    // Maximum files per folder
    private static final int MAX_FILES_PER_FOLDER = 20;

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    public FileManagerService(StorageProperties properties) throws Exception {
        if(properties.getLocation().trim().length() == 0){
            throw new Exception("File upload location can not be Empty.");
        }
        this.rootLocation = properties.getLocation();
        objectMapper = new ObjectMapper();
    }

    public String generateFilePath(String originalFilename, String hashName) throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));

        // Base path: Year/Month
        String basePath = year + "/" + month;

        // Find the appropriate folder number
        int folderNumber = findAvailableFolder(basePath);

        // Complete path structure
        String folderName = "folder-" + folderNumber;
        String completePath = basePath + "/" + folderName;

        // Return the complete file path
        return completePath + "/" + hashName + ".bin";
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

    /**
     * Gets the final storage path (local or SFTP base path)
     */
    private String getFinalStoragePath(String relativePath) {
        if (sftpEnabled) {
            return remoteBasePath + "/" + relativePath;
        } else {
            return Paths.get(rootLocation, relativePath).toString();
        }
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

        int count;
        if (sftpEnabled) {
            count = countFilesInSFTPFolder(fullPath);
        } else {
            count = countFilesInLocalFolder(fullPath);
        }

        // Update cache
        folderFileCountCache.put(relativePath, new AtomicInteger(count));
        return count;
    }

    /**
     * Counts files in local folder
     */
    private int countFilesInLocalFolder(String folderPath) throws Exception {
        Path path = Paths.get(folderPath);

        if (!Files.exists(path)) {
            return 0;
        }

        try (Stream<Path> files = Files.list(path)) {
            return (int) files.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            throw new Exception("Failed to count files in local folder: " + folderPath, e);
        }
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
     * Creates directory structure (works for both local and SFTP)
     */
    private void createDirectoryStructure(String path) throws Exception {
        if (sftpEnabled) {
            createSFTPDirectoryStructure(path);
        } else {
            createLocalDirectoryStructure(path);
        }
    }

    /**
     * Creates local directory structure
     */
    private void createLocalDirectoryStructure(String path) throws Exception {
        Path dirPath = Paths.get(path).getParent();
        if (dirPath != null) {
            Files.createDirectories(dirPath);
        }
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
        createSFTPDirectoryStructure(remoteBasePath + "/" + filePath);

        // SFTP upload
        String remotePath = remoteBasePath + "/" + filePath;
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

    /**
     * Gets file path from hash (updated to work with new structure)
     */
    public String getFilePathFromHash(String hash) throws Exception {
        FileManager file = fileRepository.findByHashName(hash)
                .orElseThrow(() -> new Exception("File with hash " + hash + " not found"));

        // For files stored with the new structure, we need to search for them
        // This is a fallback method for files that might not have path stored in DB
        LocalDateTime createdDate = file.getCreatedDate();
        String year = createdDate.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = createdDate.format(DateTimeFormatter.ofPattern("MM"));

        return findFileInYearMonth(year, month, hash + ".bin");
    }

    /**
     * Searches for a file in year/month folder structure
     */
    private String findFileInYearMonth(String year, String month, String filename) throws Exception {
        String basePath = year + "/" + month;
        int folderNumber = 0;

        while (folderNumber < 1000) { // Safety limit
            String folderPath = basePath + "/folder-" + folderNumber;
            String fullPath = getFinalStoragePath(folderPath) + "/" + filename;

            if (sftpEnabled) {
                if (fileExistsOnSFTP(remoteBasePath + "/" + folderPath + "/" + filename)) {
                    return folderPath + "/" + filename;
                }
            } else {
                Path localPath = Paths.get(rootLocation, folderPath, filename);
                if (Files.exists(localPath)) {
                    return folderPath + "/" + filename;
                }
            }

            folderNumber++;
        }

        throw new Exception("File not found: " + filename);
    }

    /**
     * Checks if file exists on SFTP server
     */
    private boolean fileExistsOnSFTP(String remotePath) {
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

            channel.stat(remotePath);
            return true;

        } catch (Exception e) {
            return false;
        } finally {
            if (channel != null && channel.isConnected()) {
                channel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
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
                String filePath = generateFilePath(data[i].getFilename(), hash);

                // Create FileManager entity
                FileManager fileManager = FileManager.builder()
                        .documentType(fileData.getDocumentType())
                        .folderID(targetDirectory.getFolderID())
                        .fileLink(filePath)
                        .hashName(hash)
                        .filename(file.getOriginalFilename())
                        .documentName(fileData.getDocumentName())
                        .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                        .metadata(fileData.getMetadata())
                        .build();

                // Save to get ID and timestamp
                fileManager = fileRepository.save(fileManager);




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
        String filePath = generateFilePath(data.getFilename(), hash);
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
                .fileLink(filePath)
                .documentName(data.getDocumentName())
                .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .metadata(data.getMetadata())
                .build();

        // Save to get ID and timestamp
        fileManager = fileRepository.save(fileManager);


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
        String filePath = generateFilePath(data.getFilename(), hash);
        storeFileViaSFTPWithPath(file, filePath);

        // Create FileManager entity
        FileManager fileManager = FileManager.builder()
                .documentType(data.getDocumentType())
                .documentName(data.getDocumentName())
                .folderID(directory.getFolderID())
                .filename(file.getOriginalFilename())
                .fileLink(filePath)
                .hashName(hash)
                .metadata(data.getMetadata())
                .mimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();

        // Save to get ID and timestamp
        fileManager = fileRepository.save(fileManager);

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
                String filePath = generateFilePath(fileManager.getFilename(), hash);
                fileManager.setFileLink(filePath);
                storeFileViaSFTPWithPath(file, filePath);

                // Update the FileManager with file-specific information
                fileManager.setFilename(file.getOriginalFilename());
                fileManager.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
                fileManager.setFolderID(directory.getFolderID());

                // Save to get ID and timestamp
                fileManager = fileRepository.save(fileManager);



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

    /**
     * Get file link by hash - updated for SFTP support
     */
    public String getFileLinkByHash(String hash) throws Exception {
        FileManager file = fileRepository.findByHashName(hash)
                .orElseThrow(() -> new Exception("File with hash " + hash + " not found"));

        if (sftpEnabled) {
            // Return SFTP-based file path
            String extension = getFileExtension(file.getFilename());
            return "/files-manager/" + hash + extension;
        } else {
            // Return local file path
            String extension = getFileExtension(file.getFilename());
            return "/files-manager/" + hash + extension;
        }
    }

    /**
     * Get encrypted file path - updated for structured path and SFTP support
     */
    public Path getEncryptedFilePath(String hash) throws Exception {
        FileManager file = fileRepository.findByHashName(hash)
                .orElseThrow(() -> new Exception("File with hash " + hash + " not found"));

        if (sftpEnabled) {
            // For SFTP, download the file to a temporary location
            return downloadFileFromSFTPByPath(file.getFileLink());
        } else {
            // For local storage, use the structured path
            String filePath = file.getFileLink();
            if (filePath == null || filePath.isEmpty()) {
                // Fallback: try to find the file using the old method
                filePath = findFileInYearMonth(
                        file.getCreatedDate().format(DateTimeFormatter.ofPattern("yyyy")),
                        file.getCreatedDate().format(DateTimeFormatter.ofPattern("MM")),
                        hash + ".bin"
                );
            }

            Path localPath = Paths.get(rootLocation).resolve(filePath);
            if (!Files.exists(localPath)) {
                throw new Exception("File not found at path: " + localPath);
            }
            return localPath;
        }
    }

    /**
     * Download file from SFTP server using the stored file path
     */
    private Path downloadFileFromSFTPByPath(String filePath) throws Exception {
        if (filePath == null || filePath.isEmpty()) {
            throw new Exception("File path is null or empty");
        }

        String remoteFilePath = remoteBasePath + "/" + filePath;
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

    /**
     * Download file from SFTP server for local processing
     */
    private Path downloadFileFromSFTP(String hash, String extension) throws Exception {
        String remoteFilePath = remoteBasePath + "/" + hash + extension;
        Path tempFile = Files.createTempFile("sftp_download_", extension);

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

    @Override
    public void init() throws Exception {
        try {
            if (!sftpEnabled) {
                Path root = Paths.get(rootLocation);
                Files.createDirectories(root);
            }
        }
        catch (IOException e) {
            throw new Exception("Could not initialize storage", e);
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

        if (sftpEnabled) {
            // Delete from SFTP server
            deleteFileFromSFTP(file.getHashName(), getFileExtension(file.getFilename()));
        } else {
            // Delete from local filesystem
            Path filePath = Paths.get(this.rootLocation)
                    .resolve(file.getHashName() + getFileExtension(file.getFilename()))
                    .normalize()
                    .toAbsolutePath();
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                throw new Exception("Failed to delete the file from the filesystem.", e);
            }
        }

        fileRepository.delete(file);
    }

    /**
     * Delete file from SFTP server
     */
    private void deleteFileFromSFTP(String hash, String extension) throws Exception {
        String remoteFilePath = remoteBasePath + "/" + hash + extension;

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
                Path encryptedFilePath = getEncryptedFilePath(file.getHashName());
                
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
                    Files.deleteIfExists(tempFile);
                    // Clean up temporary SFTP downloads
                    if (sftpEnabled && encryptedFilePath.toString().contains("sftp_download_")) {
                        Files.deleteIfExists(encryptedFilePath);
                    }
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
package com.edms.file_management.filemanager;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import com.edms.file_management.comment.Comment;
import com.edms.file_management.comment.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
public class FileManagerService implements StorageService  {
    
   private final String rootLocation;

   @Autowired
   private DirectoryService directoryService;

   @Autowired
   private DirectoryRepository directoryRepository;

    @Autowired
    private CommentRepository commentRepository;

   @Autowired
   private FileManagerRepository fileRepository;
    @Autowired
   private final ObjectMapper objectMapper;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Autowired
	public FileManagerService(StorageProperties properties) throws Exception {
        if(properties.getLocation().trim().length() == 0){
            throw new Exception("File upload location can not be Empty."); 
        }
		this.rootLocation = properties.getLocation();
        objectMapper = new ObjectMapper();
	}

	@Override
public void bulkStore(FileManager[] data, MultipartFile[] files) throws Exception {
    List<String> fileNames = new ArrayList<>();
    try {
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new Exception("Failed to store empty file.");
            }
            fileNames.add(file.getOriginalFilename());
        }
        for (int x = 0; x < files.length; x++) {
            Optional<Directory> directory = directoryRepository.findByName(data[x].getDocumentType());
            FileManager fileManager;
            if (directory.isPresent()) {
                fileManager = FileManager.builder()
                    .documentType(data[x].getDocumentType())
                    .folderID(directory.get().getFolderID())
                    .filename(files[x].getOriginalFilename())
                        .mimeType(data[x].getMimeType())
                    .build();
                fileRepository.save(fileManager);
            } else {
                Directory newDirectory = Directory.builder().name(data[x].getDocumentType()).build();
                newDirectory = directoryService.creaDirectoryWithName(newDirectory);
                fileManager = FileManager.builder()
                    .documentType(data[x].getDocumentType())
                    .folderID(newDirectory.getFolderID())
                    .filename(files[x].getOriginalFilename())
                        .documentName(data[x].getDocumentName())
                    .build();
                fileRepository.save(fileManager);
            }
            String hash = HashUtil.generateHash(fileManager.getFilename(), fileManager.getCreatedDate());
             // Resolve the destination file within the folder path
			Path destinationFile = Paths.get(this.rootLocation)
            .resolve(hash + getFileExtension(files[x].getOriginalFilename()))
            .normalize()
            .toAbsolutePath();
    

        // Create directories if they don't exist
        Files.createDirectories(destinationFile.getParent());

         // Encrypt the file before storing it
        try (InputStream inputStream = files[x].getInputStream();
        OutputStream outputStream = Files.newOutputStream(destinationFile)) {
        EncryptionUtil.encrypt(inputStream, outputStream);
    }
            fileManager.setHashName(HashUtil.generateHash(fileManager.getFilename(), fileManager.getCreatedDate()));
            fileRepository.save(fileManager);
        }
    } catch (Exception e) {
        throw new Exception("Failed to store files.", e);
    }
}

    @Override
    public void store(FileManager data, MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Failed to store empty file.");
        }

        // Fetch or create the directory entity
        Optional<Directory> directory = directoryRepository.findByName(data.getDocumentType());
        FileManager fileManager;

        if (directory.isPresent()) {
            fileManager = FileManager.builder()
                    .documentType(data.getDocumentType())
                    .folderID(directory.get().getFolderID())
                    .filename(file.getOriginalFilename())
                    .documentName(data.getDocumentName())
                    .mimeType(data.getMimeType())
                    .hashName(data.getHashName())
                    .metadata(data.getMetadata())
                    .build();
            fileRepository.save(fileManager);
        } else {
            // Create new directory if it doesn't exist
            Directory newDirectory = Directory.builder()
                    .name(data.getDocumentType())
                    .build();

            newDirectory = directoryService.creaDirectoryWithName(newDirectory);
            newDirectory.setParentFolderID((int) newDirectory.getFolderID());
            directoryRepository.save(newDirectory);

            fileManager = FileManager.builder()
                    .documentType(data.getDocumentType())
                    .folderID(newDirectory.getFolderID())
                    .filename(file.getOriginalFilename())
                    .documentName(data.getDocumentName())
                    .mimeType(data.getMimeType())
                    .hashName(data.getHashName())
                    .metadata(data.getMetadata())
                    .build();
            fileRepository.save(fileManager);
        }

        // Generate the file hash
        String hash = HashUtil.generateHash(fileManager.getFilename(), fileManager.getCreatedDate());

        // Resolve the destination file within the folder path
        Path destinationFile = Paths.get(this.rootLocation)
                .resolve(hash + getFileExtension(file.getOriginalFilename()))
                .normalize()
                .toAbsolutePath();

        // Create directories if they don't exist
        Files.createDirectories(destinationFile.getParent());

        // Encrypt the file before storing it
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(destinationFile)) {
            EncryptionUtil.encrypt(inputStream, outputStream);
        }

        // Save the file hash name
        fileManager.setHashName(hash);
        fileRepository.save(fileManager);
    }

    public FileManager storeById(FileManager data, MultipartFile file, Long folderId) throws Exception {
        if (file.isEmpty()) {
            throw new Exception("Failed to store empty file.");
        }

        // Fetch or create the directory entity
        Optional<Directory> directory = directoryRepository.findById(folderId);
        FileManager fileManager;
        if (directory.isPresent()) {
            fileManager = FileManager.builder()
                    .documentType(data.getDocumentType())
                    .documentName(data.getDocumentName())
                    .folderID(directory.get().getFolderID())
                    .filename(file.getOriginalFilename())
                    .metadata(data.getMetadata())
                    .mimeType(data.getMimeType())
                    .build();
            fileRepository.save(fileManager);
        } else {
           throw  new Exception("Folder with id " + folderId +" doesn't exist!");
        }

        // Generate the file hash
        String hash = HashUtil.generateHash(fileManager.getFilename(), fileManager.getCreatedDate());

        // Resolve the destination file within the folder path
        Path destinationFile = Paths.get(this.rootLocation)
                .resolve(hash + getFileExtension(file.getOriginalFilename()))
                .normalize()
                .toAbsolutePath();

        // Create directories if they don't exist
        Files.createDirectories(destinationFile.getParent());

        // Encrypt the file before storing it
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(destinationFile)) {
            EncryptionUtil.encrypt(inputStream, outputStream);
        }

        // Save the file hash name
        fileManager.setHashName(hash);
        return fileRepository.save(fileManager);
    }

    public void bulkStoreById(List<FileManager> fileManagers, MultipartFile[] files, Long folderId) throws Exception {
        if (files.length != fileManagers.size()) {
            throw new Exception("Mismatch between file count and metadata count.");
        }

        List<String> fileNames = new ArrayList<>();

        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                FileManager fileManager = fileManagers.get(i);

                if (file.isEmpty()) {
                    throw new Exception("Failed to store empty file: " + file.getOriginalFilename());
                }

                fileNames.add(file.getOriginalFilename());

                // Find the directory based on folder ID
                Optional<Directory> directory = directoryRepository.findById(folderId);
                if (directory.isEmpty()) {
                    throw new Exception("Folder with ID " + folderId + " not found.");
                }

                // Update the FileManager with file-specific information
                fileManager.setFilename(file.getOriginalFilename());
                fileManager.setMimeType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
                fileManager.setFolderID(directory.get().getFolderID());

                // Save the initial FileManager entry
                fileRepository.save(fileManager);

                // Generate hash for filename
                String hash = HashUtil.generateHash(fileManager.getFilename(), fileManager.getCreatedDate());
                fileManager.setHashName(hash);

                // Define file storage path
                Path destinationFile = Paths.get(this.rootLocation)
                        .resolve(hash + getFileExtension(file.getOriginalFilename()))
                        .normalize()
                        .toAbsolutePath();

                Files.createDirectories(destinationFile.getParent());

                // Encrypt and store file
                try (InputStream inputStream = file.getInputStream();
                     OutputStream outputStream = Files.newOutputStream(destinationFile)) {
                    EncryptionUtil.encrypt(inputStream, outputStream);
                }

                // Save the updated FileManager with hash
                fileRepository.save(fileManager);
            }
        } catch (Exception e) {
            throw new Exception("Failed to store files: " + e.getMessage(), e);
        }
    }

    private String getFileExtension(String filename) {
    if (filename == null) {
        return "";
    }
    int dotIndex = filename.lastIndexOf('.');
    return (dotIndex == -1) ? "" : filename.substring(dotIndex);
}

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

    public  List<FileManager> getAllFiles(Long folderId) {
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
				throw new Exception(
						"Could not read file: " + filename);

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

    public Path getEncryptedFilePath(String hash) {
        FileManager file = fileRepository.findByHashName(hash).orElseThrow();
        return Paths.get(this.rootLocation)
            .resolve(hash + getFileExtension(file.getFilename()))
            .normalize()
            .toAbsolutePath();
    }

    public void decryptFile(Path encryptedFilePath, OutputStream outputStream) throws Exception {
        try (InputStream inputStream = Files.newInputStream(encryptedFilePath)) {
            if (inputStream == null) {
                throw new IOException("Unable to create input stream for encrypted file");
            }

            // Use the decryption logic from EncryptionUtil
            EncryptionUtil.decrypt(inputStream, outputStream);
        } catch (Exception e) {
            throw new Exception("Failed to decrypt file: " + e.getMessage(), e);
        }
    }

	@Override
	public void init() throws Exception {
		try {
			Path root = Paths.get(rootLocation);
			Files.createDirectories(root);
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
            // Convert the JSON string to a FileManager object
            return objectMapper.readValue(fileData, FileManager.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON string to FileManager: " + e.getMessage(), e);
        }
    }

    public void deleteFileById(Long id) throws Exception {
        // Find the file in the repository
        FileManager file = fileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + id));

        // Get the physical file's path
        Path filePath = Paths.get(this.rootLocation)
                .resolve(file.getHashName() + getFileExtension(file.getFilename()))
                .normalize()
                .toAbsolutePath();

        // Delete the file from the filesystem
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new Exception("Failed to delete the file from the filesystem.", e);
        }

        // Remove the file from the repository
        fileRepository.delete(file);
    }

    @Transactional
    public FileManager updateMetadata(Long fileId, Map<String, Object> newMetadata) {
        FileManager file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        file.getMetadata().putAll(newMetadata); // Add or update metadata
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public FileManager deleteMetadata(Long fileId, List<String> keys) {
        FileManager file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        for (String key : keys) {
            file.getMetadata().remove(key); // Remove metadata keys
        }
        fileRepository.save(file);
        return file;
    }

    @Transactional
    public FileManager clearMetadata(Long fileId) {
        FileManager file = fileRepository.findById(fileId)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with ID: " + fileId));

        file.setMetadata(null); // Clear all metadata
        fileRepository.save(file);
        return file;
    }

    public List<FileManager> getAllFiles() {
        return fileRepository.findAll(); // Assuming the repository has a findAll() method.
    }

    public List<FileManager> fullTextSearch(String searchTerm) throws Exception {
        List<FileManager> results = new ArrayList<>();
        List<FileManager> allFiles = getAllFiles();

        for (FileManager file : allFiles) {
            // Skip files without hash names
            if (file.getHashName() == null || file.getHashName().isEmpty()) {
                continue;
            }

            // Check if file is of supported type
            String mimeType = file.getMimeType();
            String extension = getFileExtension(file.getFilename()).toLowerCase();

            // Skip unsupported file types
            if (!isSearchableDocument(mimeType, extension)) {
                continue;
            }

            // Get path to encrypted file
            Path encryptedFilePath = getEncryptedFilePath(file.getHashName());

            // Check if file exists
            if (!Files.exists(encryptedFilePath)) {
                continue;
            }

            // Create temporary file for decrypted content
            Path tempFile = Files.createTempFile("decrypted_", extension);

            try (OutputStream outputStream = Files.newOutputStream(tempFile)) {
                // Decrypt the file to the temporary location
                decryptFile(encryptedFilePath, outputStream);

                // Extract text based on file type
                String fileContent = extractTextFromFile(tempFile.toFile(), mimeType, extension);

                // Check if the file contains the search term (case-insensitive)
                if (fileContent != null && fileContent.toLowerCase().contains(searchTerm.toLowerCase())) {
                    results.add(file);
                }
            } catch (Exception e) {
                // Log the error but continue processing other files
                System.err.println("Error searching in file " + file.getFilename() + ": " + e.getMessage());
            } finally {
                // Delete the temporary file
                Files.deleteIfExists(tempFile);
            }
        }

        return results;
    }


    private boolean isSearchableDocument(String mimeType, String extension) {
        if (mimeType == null) {
            mimeType = "";
        }

        // Check common MIME types for the supported document formats
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
        // Extract text based on file extension
        if (extension.equals(".pdf")) {
            return extractTextFromPdf(file);
        } else if (extension.equals(".docx")) {
            return extractTextFromDocx(file);
        } else if (extension.equals(".xlsx") || extension.equals(".xls")) {
            return extractTextFromExcel(file);
        } else {
            // Fall back to MIME type if extension doesn't match
            if (mimeType.contains("pdf")) {
                return extractTextFromPdf(file);
            } else if (mimeType.contains("word")) {
                return extractTextFromDocx(file);
            } else if (mimeType.contains("excel") || mimeType.contains("spreadsheet")) {
                return extractTextFromExcel(file);
            }
        }

        // If we can't determine the file type or it's not supported
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
                                // Skip other cell types
                                break;
                        }
                    }
                    textContent.append("\n");
                }
            }
        }

        return textContent.toString();
    }

    public Comment saveComment(Long documentId, Long userId, String content) {
        Comment comment = Comment.builder()
                .documentId(documentId)
                .userId(userId)
                .content(content)
                .build();
        return commentRepository.save(comment);
    }

    // Fetch comments by document ID
    public List<Comment> getCommentsByDocumentId(Long documentId) {
        return commentRepository.findByDocumentId(documentId);
    }
}

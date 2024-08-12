package com.cosek.edms.filemanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.crypto.SecretKey;

import com.cosek.edms.exception.ResourceNotFoundException;
import com.cosek.edms.search.Search;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cosek.edms.config.StorageProperties;
import com.cosek.edms.directory.Directory;
import com.cosek.edms.directory.DirectoryRepository;
import com.cosek.edms.directory.DirectoryService;
import com.cosek.edms.helper.EncryptionUtil;
import com.cosek.edms.helper.HashUtil;




@Service
public class FileManagerService implements StorageService  {
    
   private final String rootLocation;

   @Autowired
   private DirectoryService directoryService;

   @Autowired
   private DirectoryRepository directoryRepository;

   @Autowired
   private FileManagerRepository fileRepository;

   private final SecretKey secretKey;

	@Autowired
	public FileManagerService(StorageProperties properties) throws Exception {
        if(properties.getLocation().trim().length() == 0){
            throw new Exception("File upload location can not be Empty."); 
        }
		this.rootLocation = properties.getLocation();
        this.secretKey = EncryptionUtil.generateOrLoadSecretKey();
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
                Directory newDirectory = Directory.builder().Name(data[x].getDocumentType()).build();
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
                .metadata(data.getMetadata())
                .build();
            fileRepository.save(fileManager);
        } else {
            Directory newDirectory = Directory.builder().Name(data.getDocumentType()).build();
            newDirectory = directoryService.creaDirectoryWithName(newDirectory);
            newDirectory.setParentFolderID((int) newDirectory.getFolderID());
            directoryRepository.save(newDirectory);
            fileManager = FileManager.builder()
                .documentType(data.getDocumentType())
                .folderID(newDirectory.getFolderID())
                .filename(file.getOriginalFilename())
                    .documentName(data.getDocumentName())
                    .mimeType(data.getMimeType())
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

    public void storeById(FileManager data, MultipartFile file, Long folderId) throws Exception {
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
        fileRepository.save(fileManager);
    }

    public void bulkStoreById(MultipartFile[] files, Long folderId) throws Exception {
        List<String> fileNames = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    throw new Exception("Failed to store empty file.");
                }
                fileNames.add(file.getOriginalFilename());
            }
            for (int x = 0; x < files.length; x++) {
                Optional<Directory> directory = directoryRepository.findById(folderId);
                FileManager fileManager;
                if (directory.isPresent()) {
                    fileManager = FileManager.builder()
                            .folderID(directory.get().getFolderID())
                            .filename(files[x].getOriginalFilename())
                            .mimeType(files[x].getContentType())
                            .build();
                    fileRepository.save(fileManager);
                } else {
                    throw  new Exception("Folder with id " + folderId +" doesn't exist!");
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
            EncryptionUtil.decrypt(inputStream, outputStream);
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
}

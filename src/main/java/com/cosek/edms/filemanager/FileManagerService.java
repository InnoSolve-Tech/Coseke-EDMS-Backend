package com.cosek.edms.filemanager;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cosek.edms.config.StorageProperties;
import com.cosek.edms.directory.DirectoryService;




@Service
public class FileManagerService implements StorageService  {
    
   private final String rootLocation;

   @Autowired
   private DirectoryService directoryService;

   @Autowired
   private FileManagerRepository fileRepository;

	@Autowired
	public FileManagerService(StorageProperties properties) throws Exception {
        if(properties.getLocation().trim().length() == 0){
            throw new Exception("File upload location can not be Empty."); 
        }
		this.rootLocation = properties.getLocation();
	}

	@Override
	public void bulkStore(String[] dates, MultipartFile[] files, Long folderID) throws Exception {
    List<String> fileNames = new ArrayList<>();
    try {
        // Get the folder path based on the folderID
        String folderPath = directoryService.getDirectoryPath(folderID);
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                throw new Exception("Failed to store empty file.");
            }
            
            // Resolve the destination file within the root location and folderPath
            Path destinationFile = Paths.get(folderPath)
                    .resolve(file.getOriginalFilename())
                    .normalize()
                    .toAbsolutePath();
            
            // Create directories if they don't exist
            Files.createDirectories(destinationFile.getParent()); // Create parent directories if not exist
            
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
            fileNames.add(file.getOriginalFilename());
        }
		for (int x = 0; x < files.length; x++) {
			FileManager fileManager = FileManager.builder().dateUploaded(dates[x]).filename(files[x].getOriginalFilename()).folderID(folderID).build();
			fileRepository.save(fileManager);
		}
    } catch (Exception e) {
        throw new Exception("Failed to store files.", e);
    }
}

	@Override
	public void store(String date, MultipartFile file, Long folderID) throws Exception {
		try {
			if (file.isEmpty()) {
				throw new Exception("Failed to store empty file.");
			}
			
			// Get the folder path based on the folderID
			String folderPath = directoryService.getDirectoryPath(folderID);
			
			// Resolve the destination file within the folder path
			Path destinationFile = Paths.get(folderPath)
				.resolve(file.getOriginalFilename())
				.normalize()
				.toAbsolutePath();
			
			// Security check to ensure the destination file is within the folder path
			if (!destinationFile.getParent().equals(Paths.get(folderPath).toAbsolutePath())) {
				throw new Exception("Cannot store file outside current directory.");
			}
	
			// Create directories if they don't exist
			Files.createDirectories(destinationFile.getParent());
	
			try (InputStream inputStream = file.getInputStream()) {
				Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
			}
			FileManager fileManager = FileManager.builder().dateUploaded(date).filename(file.getOriginalFilename()).folderID(folderID).build();
			fileRepository.save(fileManager);
		} catch (Exception e) {
			throw new Exception("Failed to store file.", e);
		}
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
}

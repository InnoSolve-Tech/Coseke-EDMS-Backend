package com.cosek.edms.directory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cosek.edms.config.StorageProperties;

@Service
public class DirectoryService {
     
    @Autowired
    private DirectoryRepository directoryRepository;

    private final String rootLocation;
    @Autowired
	public DirectoryService(StorageProperties properties) throws Exception {
        if(properties.getLocation().trim().length() == 0){
            throw new Exception("File upload location can not be Empty."); 
        }
		this.rootLocation = properties.getLocation();
	}

    public String getDirectoryPath(Long folderID) {
        String filePath = "";
        Directory folder = directoryRepository.findById(folderID)
        .orElseThrow(() -> new IllegalArgumentException("Folder not found with id " + folderID));
        while(folder.getFolderID() != folder.getParentFolderID()) {
            filePath = folder.getName()+"/"+filePath;
            folder = directoryRepository.findById((long) folder.getParentFolderID()).orElseThrow(() -> new IllegalArgumentException("Folder not found with id " + folderID));;
        }
        filePath = rootLocation+"/"+folder.getName()+"/"+filePath;
        return filePath;
    }   

    public Directory getDirectoryById(Long folderID) throws Exception {
        return directoryRepository.findById(folderID)
                .orElseThrow(() -> new Exception("Directory not found for this id :: " + folderID));
    }

    public List<Directory> getAllDirectories() {
        return directoryRepository.findAll();
    }

    public Directory createDirectory(Directory directory) {
        return directoryRepository.save(directory);
    }

    public Directory creaDirectoryWithName(Directory directory) {
        Directory newDirectory = new Directory();
        newDirectory.setName(directory.getName());
        System.out.println("Created Directory with name: " + directory.getName());
        return directoryRepository.save(newDirectory);
    }

    public Directory createSubDirectory(Directory directory) {
        Directory newFolder = new Directory();
        newFolder.setName(directory.getName());
        newFolder.setParentFolderID(directory.getParentFolderID());
        System.out.println("Created Directory with name: " + directory.getName());
        return directoryRepository.save(newFolder);
    }

    public void deleteDirectoryById(Long directoryId) {
        directoryRepository.deleteById(directoryId);
    }

    public Directory updateDirectoryParent(Long directoryID, Directory directoryRequest) {
        Directory directory = directoryRepository.findById(directoryID)
        .orElseThrow(() -> new IllegalArgumentException("Directory not found with id " + directoryID));
        directory.setParentFolderID(directoryRequest.getParentFolderID());
        return directoryRepository.save(directory);
    }
}

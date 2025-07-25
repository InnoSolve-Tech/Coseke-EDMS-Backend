package com.edms.file_management.directory;

import com.edms.file_management.config.StorageProperties;
import com.edms.file_management.directoryAccessControl.AccessType;
import com.edms.file_management.directoryAccessControl.DirectoryAccessControl;
import com.edms.file_management.directoryAccessControl.DirectoryAccessControlRepository;
import com.edms.file_management.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectoryService {
     
    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryAccessControlRepository accessControlRepository;

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

    @Transactional
    public Directory updateDirectory(Directory directory) throws Exception{
        Directory folder = directoryRepository.findById((long) directory.getFolderID()).orElseThrow(() -> new RuntimeException("Failed to find folder!"));
        if(folder.getAccessControl() != null) {
            if(directory.getAccessControl().getAccessType() != null) {
                DirectoryAccessControl accessControl = accessControlRepository.save(directory.getAccessControl());
                folder.setAccessControl(accessControl);
                folder.setName(directory.getName());
                return directoryRepository.save(folder);
            } else {
                throw new RuntimeException("Access type should not be null!");
            }
        } else {
            if(directory.getAccessControl().getAccessType() != null) {
                directory.getAccessControl().setDirectory(folder);
                folder.setName(directory.getName());
                DirectoryAccessControl accessControl = accessControlRepository.save(directory.getAccessControl());
                folder.setAccessControl(accessControl);
                return directoryRepository.save(folder);
            } else {
                throw new RuntimeException("Access type should not be null!");
            }
        }
    }

    public List<Directory> getAllDirectories() {
        return directoryRepository.findAllWithFiles();
    }

    public Directory createDirectory(Directory directory) {
        DirectoryAccessControl accessControl = DirectoryAccessControl.builder().accessType(AccessType.PUBLIC).build();
        accessControl = accessControlRepository.save(accessControl);
        directory.setAccessControl(accessControl);
        directory = directoryRepository.save(directory);
        accessControl.setDirectory(directory);
        accessControlRepository.save(accessControl);
        return directory;
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

    public List<Directory> getDirectoriesByParentId(int parentId, int maxDepth) {
        List<Directory> result = new ArrayList<>();
        recursivelyFindChildDirectories(parentId, result, 0, maxDepth);
        return result;
    }

    private void recursivelyFindChildDirectories(int parentId, List<Directory> result, int currentDepth, int maxDepth) {
        if (currentDepth >= maxDepth) {
            return;
        }

        List<Directory> childDirectories = directoryRepository.findByParentFolderID(parentId);
        result.addAll(childDirectories);

        for (Directory child : childDirectories) {
            recursivelyFindChildDirectories(child.getFolderID(), result, currentDepth + 1, maxDepth);
        }
    }

    public void renameDirectory(Long folderId, String newName) throws Exception {
        // Validate new folder name
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be empty.");
        }

        // Find the directory by ID
        Directory directory = directoryRepository.findById(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with ID: " + folderId));

        // Update the folder name
        directory.setName(newName);
        directoryRepository.save(directory);
    }

}

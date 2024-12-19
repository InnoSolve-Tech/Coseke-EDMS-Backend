package com.edms.file_management.directory;

import com.edms.file_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/directories")
public class DirectoryController {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryService directoryService;

    @GetMapping("/all")
    public List<Directory> getAllDirectories() {
        return directoryService.getAllDirectories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Directory> getDirectoryById(@PathVariable(value = "id") Long directoryId)
            throws Exception {
        Directory directory = directoryService.getDirectoryById(directoryId);
        return ResponseEntity.ok().body(directory);
    }

    @PostMapping("/create")
    public Directory createDirectory(@RequestBody Directory directory) {
        System.out.println("Created Directory: " + directory);
        return directoryService.createDirectory(directory);
    }
    
    @PostMapping("/create-with-name")
    public Directory createDirectoryWithName(@RequestBody Directory directory) {
       return directoryService.creaDirectoryWithName(directory);
    }

    @PostMapping("/subfolder")
    public Directory createSubFolder(@RequestBody Directory directory) {
      return directoryService.createSubDirectory(directory);
    }

    @GetMapping("/last")
    public List<Directory> searchDocuments() {
        return directoryRepository.findByLastInput();
    }

    @PutMapping("/{folderID}")
    public Directory updateFolderParent(@PathVariable Long folderID, @RequestBody Directory directoryRequest) {
        return directoryService.updateDirectoryParent(folderID, directoryRequest);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDirectory(@PathVariable Long id) {
        try {
            directoryService.deleteDirectoryById(id);
            return ResponseEntity.ok("Folder successfully deleted.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to delete folder: " + e.getMessage());
        }
    }


    @GetMapping("/by-parent/{parentId}/{maxDepth}")
    public ResponseEntity<List<Directory>> getDirectoriesByParentId(
            @PathVariable(value = "parentId") int parentId,
            @PathVariable(value = "maxDepth") int maxDepth) {
        List<Directory> childDirectories = directoryService.getDirectoriesByParentId(parentId, maxDepth);
        return ResponseEntity.ok().body(childDirectories);
    }

    @PutMapping("/edit/{folderId}")
    public ResponseEntity<String> renameDirectory(@PathVariable Long folderId, @RequestBody Directory directoryRequest) {
        try {
            directoryService.renameDirectory(folderId, directoryRequest.getName());
            return ResponseEntity.ok("Folder renamed successfully.");
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Folder not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to rename folder: " + e.getMessage());
        }
    }

}


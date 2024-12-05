package com.edms.file_management.directory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/folders")
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
    
    @GetMapping("/directory/delete/{directoryId}")
    public void deleteFolder(@PathVariable Long directoryID) {
        directoryService.deleteDirectoryById(directoryID);
    }

    @GetMapping("/by-parent/{parentId}/{maxDepth}")
    public ResponseEntity<List<Directory>> getDirectoriesByParentId(
            @PathVariable(value = "parentId") int parentId,
            @PathVariable(value = "maxDepth") int maxDepth) {
        List<Directory> childDirectories = directoryService.getDirectoriesByParentId(parentId, maxDepth);
        return ResponseEntity.ok().body(childDirectories);
    }

}


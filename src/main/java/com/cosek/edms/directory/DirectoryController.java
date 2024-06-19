package com.cosek.edms.directory;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:3000")
public class DirectoryController {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryService directoryService;

    @GetMapping("/directories")
    public List<Directory> getAllDirectories() {
        return directoryService.getAllDirectories();
    }

    @GetMapping("/directories/{id}")
    public ResponseEntity<Directory> getDirectoryById(@PathVariable(value = "id") Long directoryId)
            throws Exception {
        Directory directory = directoryService.getDirectoryById(directoryId);
        return ResponseEntity.ok().body(directory);
    }

    @PostMapping("/directories")
    public Directory createDirectory(@RequestBody Directory directory) {
        System.out.println("Created Directory: " + directory);
        return directoryService.createDirectory(directory);
    }
    
    @PostMapping("/directories/create")
    public Directory createDirectoryWithName(@RequestBody Directory directory) {
       return directoryService.creaDirectoryWithName(directory);
    }

    @PostMapping("/directories/subfolder")
    public Directory createSubFolder(@RequestBody Directory directory) {
      return directoryService.createSubDirectory(directory);
    }

    @GetMapping("/directories/last")
    public List<Directory> searchDocuments() {
        return directoryRepository.findByLastInput();
    }

    @PutMapping("/directories/{folderID}")
    public Directory updateFolderParent(@PathVariable Long folderID, @RequestBody Directory directoryRequest) {
        return directoryService.updateDirectoryParent(folderID, directoryRequest);
    }
    
    @GetMapping("/directory/delete/{directoryId}")
    public void deleteFolder(@PathVariable Long directoryID) {
        directoryService.deleteDirectoryById(directoryID);
    } 
}

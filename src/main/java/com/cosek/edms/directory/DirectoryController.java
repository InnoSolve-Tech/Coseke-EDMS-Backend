package com.cosek.edms.directory;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@RequestMapping("/api/v1/directories")
@CrossOrigin(origins = "http://localhost:3000")
public class DirectoryController {

    @Autowired
    private DirectoryRepository directoryRepository;

    @Autowired
    private DirectoryService directoryService;

    @GetMapping("/")
    public List<Directory> getAllDirectories() {
        return directoryService.getAllDirectories();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Directory> getDirectoryById(@PathVariable(value = "id") Long directoryId)
            throws Exception {
        Directory directory = directoryService.getDirectoryById(directoryId);
        return ResponseEntity.ok().body(directory);
    }

    @PostMapping("/")
    public Directory createDirectory(@RequestBody Directory directory) {
        System.out.println("Created Directory: " + directory);
        return directoryService.createDirectory(directory);
    }
    
    @PostMapping("/create")
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
}

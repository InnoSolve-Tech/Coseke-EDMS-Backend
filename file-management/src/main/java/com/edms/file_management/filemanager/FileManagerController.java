package com.edms.file_management.filemanager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
@RestController
@RequestMapping("/api/v1/files")
public class FileManagerController {

    private final FileManagerService fileService;

    private final FileManagerRepository fileRepository;

    @Autowired
    public FileManagerController(FileManagerService fileService, FileManagerRepository fileRepository) {
        this.fileService = fileService;
        this.fileRepository = fileRepository;
    }

    @GetMapping("/allfiles")
    public ResponseEntity<List<FileManager>> getAllFiles() {
        try {
            List<FileManager> files = fileService.getAllFiles();
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }


    @GetMapping("/folder/{folderID}")
    public List<FileManager> listFilesByFolderId(@PathVariable Long folderID) throws Exception {
        return fileService.getAllFiles(folderID);
    }

    @GetMapping("/download/{hash}")
    public void handleFileDownload(@PathVariable String hash, HttpServletResponse response) {
        try {
            Path encryptedFile = fileService.getEncryptedFilePath(hash);
            response.setHeader("Content-Disposition", "attachment; filename=\"" + hash + "\"");
            response.setContentType("application/octet-stream");

            // Decrypt and write the file to the response's output stream
            fileService.decryptFile(encryptedFile, response.getOutputStream());
        } catch (Exception e) {
            // Log the error and set the response status before calling getOutputStream
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/plain");
            try {
                response.getOutputStream().write(("Failed to download file: " + e.getMessage()).getBytes());
                response.getOutputStream().flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }
    @GetMapping("/{folderID}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable Long folderID, @PathVariable String filename) throws Exception {
        Resource file = fileService.loadAsResource(filename, folderID);
        if (file == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public ResponseEntity<String> handleFileUpload(@RequestPart("fileData") FileManager fileData, @RequestPart("file") MultipartFile file) throws Exception {
        fileService.store(fileData, file);
        return ResponseEntity.ok().body("You successfully uploaded " + file.getOriginalFilename() + "!");
    }

    @PostMapping("/{folderId}")
    public ResponseEntity<FileManager> handleFileUploadById(
            @RequestPart("fileData") String fileData,
            @RequestPart("file") MultipartFile file,
            @PathVariable("folderId") Long folderId) throws Exception {
        System.out.println("Received fileData: " + fileData); // Log for debugging
        FileManager dataManager = fileService.convertStringToDataManager(fileData);
        FileManager newFile = fileService.storeById(dataManager, file, folderId);
        return ResponseEntity.ok().body(newFile);
    }


    @PostMapping("/bulk/{folderId}")
    public ResponseEntity<String> handleBulkFileUploadById(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("fileData") String fileData,
            @PathVariable("folderId") Long folderId) throws Exception {

        System.out.println("Received bulk upload request:");
        System.out.println("FileData: " + fileData);
        System.out.println("Number of files: " + files.length);

        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, Object>> fileDataList = mapper.readValue(fileData,
                new TypeReference<List<Map<String, Object>>>() {});

        // Convert the Map objects to FileManager objects with explicit type handling
        List<FileManager> fileManagers = fileDataList.stream()
                .map(data -> {
                    System.out.println("Processing file data: " + data);
                    return FileManager.builder()
                            .documentType(String.valueOf(data.get("documentType")))
                            .documentName(String.valueOf(data.get("documentName")))
                            .mimeType(String.valueOf(data.get("mimeType")))
                            .folderID(folderId.intValue())
                            .metadata(convertMetadata(data.get("metadata")))
                            .build();
                })
                .collect(Collectors.toList());

        fileService.bulkStoreById(fileManagers, files, folderId);

        return ResponseEntity.ok()
                .body("Successfully uploaded " + files.length + " files under folder ID: " + folderId);
    }

    // Helper method to safely convert metadata
    private Map<String, Object> convertMetadata(Object metadataObj) {
        if (metadataObj == null) {
            return new HashMap<>();
        }
        if (metadataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadata = (Map<String, Object>) metadataObj;
            return metadata;
        }
        return new HashMap<>();
    }

    @PostMapping("/file-update")
    public  ResponseEntity<FileManager> handleFileUpdate(@RequestBody FileManager fileManager) {
        return ResponseEntity.ok(fileService.updateFile(fileManager));

    }

    @PostMapping("/bulk")
    public ResponseEntity<String> handleBulkFileUpload(@RequestParam("data") FileManager[] fileData, @RequestParam("files") MultipartFile[] files) throws Exception {
        fileService.bulkStore(fileData, files);
        return ResponseEntity.ok().body("You successfully uploaded " + files.length + " files!");
    }

    @GetMapping("/stored")
    public ResponseEntity<List<FileManager>> getFilesInStore() {
        return ResponseEntity.ok(fileService.getFilesInStore());
    }

    @GetMapping("/file/{id}")
    public ResponseEntity<FileManager> getFileByID(@PathVariable Long id) {
        return ResponseEntity.ok(fileService.getFileByID(id));
    }

    @GetMapping("/file/hash/{hashname}")
    public void getFileByHashName(@PathVariable String hashname, HttpServletResponse response) {
        try {
            Path encryptedFilePath = fileService.getEncryptedFilePath(hashname);

            response.setHeader("Content-Disposition", "attachment; filename=\"" + hashname + "\"");
            response.setContentType("application/octet-stream");

            fileService.decryptFile(encryptedFilePath, response.getOutputStream());
        } catch (Exception e) {
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/plain");
            try {
                response.getOutputStream().write(("Failed to download file: " + e.getMessage()).getBytes());
                response.getOutputStream().flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @GetMapping("/search")
    public List<FileManager> searchFiles(@RequestParam String keyword) {
        return fileService.searchFiles(keyword);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFileById(id);
            return ResponseEntity.ok("File successfully deleted.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting file: " + e.getMessage());
        }
    }

    @PostMapping("/{fileId}/metadata")
    public ResponseEntity<FileManager> updateMetadata(
            @PathVariable Long fileId,
            @RequestBody Map<String, Object> newMetadata) {
        return ResponseEntity.ok(fileService.updateMetadata(fileId, newMetadata));
    }

    @DeleteMapping("/{fileId}/metadata")
    public ResponseEntity<FileManager> deleteMetadata(
            @PathVariable Long fileId,
            @RequestBody List<String> keys) {
        return ResponseEntity.ok(fileService.deleteMetadata(fileId, keys));
    }

    @DeleteMapping("/{fileId}/metadata/all")
    public ResponseEntity<FileManager> clearMetadata(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileService.clearMetadata(fileId));
    }


}

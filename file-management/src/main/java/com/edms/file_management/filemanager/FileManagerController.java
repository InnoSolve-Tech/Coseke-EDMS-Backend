package com.edms.file_management.filemanager;

import com.edms.file_management.fileAccessControl.FileAccessControl;
import com.edms.file_management.fileVersions.FileVersions;
import io.github.pixee.security.Newlines;
import io.github.pixee.security.Newlines;
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

    @GetMapping("/search/content")
    public ResponseEntity<?> fullTextSearch(@RequestParam String searchTerm) {
        try {
            List<FileManager> results = fileService.fullTextSearch(searchTerm);
            if (results == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Search returned null results");
            }
            if (results.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error during search: " + e.getMessage());
        }
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

    @PutMapping("/access/{id}")
    public ResponseEntity<FileAccessControl> updateFilePermissions(@PathVariable("id") Long id, @RequestBody FileAccessControl accessControl) throws Exception {
        return ResponseEntity.ok(fileService.updateFileAccessControl(id, accessControl));
    }

    @GetMapping("/folder/{folderID}")
    public List<FileManager> listFilesByFolderId(@PathVariable Long folderID) throws Exception {
        return fileService.getAllFiles(folderID);
    }

    @GetMapping("/download/{hash}")
    public void handleFileDownload(@PathVariable String hash, HttpServletResponse response, @RequestParam(value = "version", required = false) String version) {
        try {
            // Get file info from database
            FileManager fileManager = fileRepository.findByHashName(hash)
                    .orElseThrow(() -> new Exception("File with hash " + hash + " not found"));

            // Get the actual file path (works with both SFTP and local storage)
            Path encryptedFile = fileService.getEncryptedFilePath(hash, version);

            // Set response headers with original filename
            String originalFilename = fileManager.getFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = hash;
            }

            response.setHeader("Content-Disposition",
                    Newlines.stripAll("attachment; filename=\"" + originalFilename + "\""));
            response.setContentType(fileManager.getMimeType() != null ?
                    fileManager.getMimeType() : "application/octet-stream");

            // Decrypt and write the file to the response's output stream
            fileService.decryptFile(encryptedFile, response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            // Log the error
            System.err.println("Error downloading file with hash " + hash + ": " + e.getMessage());
            e.printStackTrace();

            // Set error response
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/plain");
            try {
                if (!response.isCommitted()) {
                    response.getOutputStream().write(
                            ("Failed to download file: " + e.getMessage()).getBytes());
                    response.getOutputStream().flush();
                }
            } catch (IOException ioException) {
                System.err.println("Error writing error response: " + ioException.getMessage());
            }
        }
    }

    @GetMapping("/file/hash/{hashname}")
    public void getFileByHashName(@PathVariable String hashname, HttpServletResponse response) {
        try {
            // Get file info from database
            FileManager fileManager = fileRepository.findByHashName(hashname)
                    .orElseThrow(() -> new Exception("File with hash " + hashname + " not found"));

            // Get the encrypted file path
            Path encryptedFilePath = fileService.getEncryptedFilePath(hashname, null);

            // Set response headers with original filename
            String originalFilename = fileManager.getFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                originalFilename = hashname;
            }

            response.setHeader("Content-Disposition",
                    Newlines.stripAll("attachment; filename=\"" + originalFilename + "\""));
            response.setContentType(fileManager.getMimeType() != null ?
                    fileManager.getMimeType() : "application/octet-stream");

            // Decrypt and stream the file
            fileService.decryptFile(encryptedFilePath, response.getOutputStream());
            response.getOutputStream().flush();

        } catch (Exception e) {
            // Log the error
            System.err.println("Error downloading file with hash " + hashname + ": " + e.getMessage());
            e.printStackTrace();

            // Set error response
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            response.setContentType("text/plain");
            try {
                if (!response.isCommitted()) {
                    response.getOutputStream().write(
                            ("Failed to download file: " + e.getMessage()).getBytes());
                    response.getOutputStream().flush();
                }
            } catch (IOException ioException) {
                System.err.println("Error writing error response: " + ioException.getMessage());
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

    @PutMapping("/")
    public ResponseEntity<String> updateFile(@RequestPart("fileData") FileVersions fileData, @RequestPart("file") MultipartFile file) throws Exception {
        fileService.updateFile(fileData, file);
        return ResponseEntity.ok().body("You successfully uploaded " + file.getOriginalFilename() + "!");
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

    @GetMapping("/search")
    public List<FileManager> searchFiles(@RequestParam String keyword) {
        return fileService.searchFiles(keyword);
    }

    @DeleteMapping("/delete/{hash}")
    public ResponseEntity<String> deleteFile(@PathVariable String hash) {
        try {
            fileService.deleteFileByHashName(hash);
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

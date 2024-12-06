package com.edms.file_management.filemanager;

import com.edms.file_management.documentType.DocumentType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Map;

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

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> handleFileUpload(
            @RequestParam("filename") String filename,
            @RequestParam("documentType") String documentType,
            @RequestParam("documentName") String documentName,
            @RequestParam("folderID") String folderID,
            @RequestParam("metadata") String metadataJson,
            @RequestParam("mimeType") String mimeType,
            @RequestParam("fileContent") String fileContent,
            @RequestParam("file") MultipartFile file
    ) {
        try {
            // Decode base64 file content (if needed)
            byte[] decodedFile = Base64.getDecoder().decode(fileContent);

            // Parse metadata
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> metadata = objectMapper.readValue(metadataJson, new TypeReference<Map<String, Object>>() {});

            // Create FileManager object
            FileManager fileManager = FileManager.builder()
                    .documentType(documentType)
                    .folderID(Integer.parseInt(folderID))
                    .filename(filename)
                    .documentName(documentName)
                    .mimeType(mimeType)
                    .metadata(metadata)
                    .build();

            // Store the file
            fileService.store(fileManager, file);

            return ResponseEntity.ok().body(Map.of("message", "Successfully uploaded " + filename));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Upload failed",
                    "details", e.getMessage()
            ));
        }
    }

    @PostMapping("/{folderId}")
    public ResponseEntity<String> handleFileUploadById(@RequestPart("fileData") FileManager fileData, @RequestPart("file") MultipartFile file, @PathVariable("folderId") Long folderId) throws Exception {
        fileService.storeById(fileData, file, folderId);
        return ResponseEntity.ok().body("You successfully uploaded " + file.getOriginalFilename() + "!");
    }

    @PostMapping("/bulk/{folderId}")
    public ResponseEntity<String> handleBulkFileUploadById(@RequestPart("files") MultipartFile[] files, @PathVariable("folderId") Long folderId) throws Exception {
        fileService.bulkStoreById(files, folderId);
        return ResponseEntity.ok().body("You successfully uploaded " + files.length+ " files!");
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
            // Get the encrypted file path using the hashname
            Path encryptedFilePath = fileService.getEncryptedFilePath(hashname);

            // Set the response headers
            response.setHeader("Content-Disposition", "attachment; filename=\"" + hashname + "\"");
            response.setContentType("application/octet-stream");

            // Decrypt and write the file to the response's output stream
            fileService.decryptFile(encryptedFilePath, response.getOutputStream());
        } catch (Exception e) {
            // Handle errors
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

}

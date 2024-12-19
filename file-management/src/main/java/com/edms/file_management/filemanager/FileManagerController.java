package com.edms.file_management.filemanager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

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
    public ResponseEntity<String> handleFileUploadById(
            @RequestPart("fileData") String fileData,
            @RequestPart("file") MultipartFile file,
            @PathVariable("folderId") Long folderId) throws Exception {
        System.out.println("Received fileData: " + fileData); // Log for debugging
        FileManager dataManager = fileService.convertStringToDataManager(fileData);
        fileService.storeById(dataManager, file, folderId);
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

}

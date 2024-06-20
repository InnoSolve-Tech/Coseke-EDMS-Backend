package com.cosek.edms.filemanager;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/files")
@CrossOrigin("*")
public class FileManagerController {

    private final FileManagerService fileService;

    @Autowired
    public FileManagerController(FileManagerService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("/{folderID}")
    public List<String> listUploadedFiles(@PathVariable Long folderID) throws Exception {
        return fileService.loadAll(folderID).map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileManagerController.class,
                        "serveFile", path.getFileName().toString(), folderID).build().toUri().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/link/{folderID}")
    public ResponseEntity<String> getFileLink(@PathVariable Long folderID, @RequestParam("filename") String filename) {
        return ResponseEntity.ok(fileService.load(filename, folderID).toString());
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

    @PostMapping("/bulk")
    public ResponseEntity<String> handleBulkFileUpload(@RequestParam("data") FileManager[] fileData, @RequestParam("files") MultipartFile[] files) throws Exception {
        fileService.bulkStore(fileData, files);
        return ResponseEntity.ok().body("You successfully uploaded " + files.length + " files!");
    }

    @GetMapping("/stored")
    public ResponseEntity<List<FileManager>> getFilesInStore() {
        return ResponseEntity.ok(fileService.getFilesInStore());
    }
}

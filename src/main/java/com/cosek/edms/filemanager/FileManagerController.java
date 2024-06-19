package com.cosek.edms.filemanager;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());
    }

    @GetMapping("/link/{folderID}")
    public ResponseEntity<String> getFileLink(@PathVariable Long folderID, @RequestParam("filename") String filename) {
        return ResponseEntity.ok(fileService.load(filename, folderID).toString());
    }

    @GetMapping("/{filename:.+}/{folderID}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename, @PathVariable Long folderID) throws Exception {
        Resource file = fileService.loadAsResource(filename, folderID);
        if (file == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/{folderID}")
    public ResponseEntity<String> handleFileUpload(@RequestParam("date") String date, @RequestParam("file") MultipartFile file, @PathVariable Long folderID) throws Exception {
        fileService.store(date, file, folderID);
        return ResponseEntity.ok().body("You successfully uploaded " + file.getOriginalFilename() + "!");
    }

	@PostMapping("/bulk/{folderID}")
    public ResponseEntity<String> handleBulkFileUpload(@RequestParam("dates") String[] dates,@RequestParam("files") MultipartFile[] files, @PathVariable Long folderID) throws Exception {
        fileService.bulkStore(dates, files, folderID);
        return ResponseEntity.ok().body("You successfully uploaded " + files.length + "files!");
    }

    @GetMapping("/stored")
    public ResponseEntity<List<FileManager>> getFilesInStore() {
        return ResponseEntity.ok(fileService.getFilesInStore());
    }
}

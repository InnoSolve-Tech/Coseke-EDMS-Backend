package com.cosek.edms.signatures;

import com.cosek.edms.config.StorageProperties;
import com.cosek.edms.helper.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/v1/signatures")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SignatureController {

    private final StorageProperties storageProperties;
    private final SignatureService signatureService;



    @GetMapping("/user")
    public ResponseEntity<List<Signature>> getByUser() {
        return ResponseEntity.ok(signatureService.getSignaturesByUser());
    }

    @GetMapping("/link/{id}")
    public ResponseEntity<String> getSignatureLinkById(@PathVariable("id") Long id) throws UnknownHostException {
        return ResponseEntity.ok(signatureService.getSignatureLinkByID(id));
    }


    @GetMapping("/{fileName:.+}")
    public void serveFile(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
        Path filePath = Paths.get(storageProperties.getLocation(), "signatures").resolve(fileName).normalize();

        if (!Files.exists(filePath)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
            return;
        }

        String contentType = Files.probeContentType(filePath);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        try (InputStream encryptedStream = Files.newInputStream(filePath);
             OutputStream outStream = response.getOutputStream()) {
            EncryptionUtil.decrypt(encryptedStream, outStream);
            outStream.flush();
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error decrypting file: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<Signature> createSignature(
            @RequestParam("signature") String signatureJson,
            @RequestParam("file") MultipartFile file
    ) throws Exception {
        Signature signature = new ObjectMapper().readValue(signatureJson, Signature.class);
        return ResponseEntity.ok(signatureService.createSignature(signature, file));
    }

}

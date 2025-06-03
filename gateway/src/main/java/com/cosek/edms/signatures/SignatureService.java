package com.cosek.edms.signatures;

import com.cosek.edms.config.StorageProperties;
import com.cosek.edms.helper.EncryptionUtil;
import com.cosek.edms.user.User;
import com.cosek.edms.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SignatureService {

    @Value("${server.port}")
    private String serverPort;
    private final String rootLocation;
    private final SignatureRepository signatureRepository;
    private final UserRepository userRepository;

    @Autowired
    public SignatureService(StorageProperties properties, SignatureRepository signatureRepository, UserRepository userRepository) throws Exception {
        if (properties.getLocation().trim().isEmpty()) {
            throw new Exception("File upload location can not be Empty.");
        }
        this.rootLocation = properties.getLocation() + "/signatures";
        this.signatureRepository = signatureRepository;
        this.userRepository = userRepository;
    }

    public String getSignatureLinkByID(Long id) throws UnknownHostException {
        Signature signature = signatureRepository.findById(id).orElseThrow();
        String ip = "localhost";
        String filePath = signature.getName();
        return "http://" + ip+ ":" + serverPort+ "/api/v1/signatures/"+ filePath;
    }


    public Signature createSignature(Signature signature, MultipartFile file) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString(); // fallback if principal is a raw string
        }
        User user = userRepository.findByEmail(email).orElseThrow();

        if (file.isEmpty()) {
            throw new Exception("Failed to store empty file.");
        }


        // Resolve the destination file within the folder path
        Path destinationFile = Paths.get(this.rootLocation)
                .resolve(signature.getName())
                .normalize()
                .toAbsolutePath();

        // Ensure parent directories exist
        Files.createDirectories(destinationFile.getParent());

        // Encrypt the file before storing it
        try (InputStream inputStream = file.getInputStream();
             OutputStream outputStream = Files.newOutputStream(destinationFile)) {
            EncryptionUtil.encrypt(inputStream, outputStream);
        }
        signature.setUser(user);
        return signatureRepository.save(signature);
    }

    public List<Signature> getSignaturesByUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email;

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString(); // fallback if principal is a raw string
        }
        System.out.println("Email: " + email);
        User user = userRepository.findByEmail(email).orElseThrow();
        List<Signature> signatures = signatureRepository.findByUser(user).orElseThrow();
        return signatures;
    }
}

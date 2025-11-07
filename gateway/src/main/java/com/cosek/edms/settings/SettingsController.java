package com.cosek.edms.settings;

import com.cosek.edms.user.User;
import com.cosek.edms.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/settings")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getSettings() {
        try {
            Optional<Settings> settings = settingsService.getSettings();
            return ResponseEntity.ok(settings.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving settings: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createOrUpdateSettings(
            @RequestPart("settings") Settings settings,
            @RequestPart(value = "logo", required = false) MultipartFile logo
    ) {
        try {
            Settings savedSettings = settingsService.createOrUpdateSettings(settings, logo);
            return ResponseEntity.ok(savedSettings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating/updating settings: " + e.getMessage());
        }
    }
}

package com.cosek.edms.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/logging")
@CrossOrigin("*")
public class LoggingController {

    @Autowired
    private LoggingService loggingService;

    @GetMapping
    public ResponseEntity<List<Logging>>  getAllLogs() {
        try {
            return ResponseEntity.ok(loggingService.parseLogEntries());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

package com.cosek.edms.logging;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class LoggingService {
    private static final String LOG_FILE_PATH = "logs/application.log";

    public List<Logging> parseLogEntries() throws IOException {
        List<Logging> logEntries = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(LOG_FILE_PATH))) {
            String line;
            Logging logging = null;

            while ((line = reader.readLine()) != null) {
                if (line.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} \\[.*?\\] \\w+ .*")) {
                    // This is a new log entry
                    if (logging != null) {
                        logEntries.add(logging);
                    }
                    logging = new Logging();
                    String[] parts = line.split(" ", 4);
                    logging.setDate(parts[0] + " " + parts[1]);
                    logging.setType(parts[3].split(" ")[0]); // Extract log level (INFO, DEBUG, etc.)
                    logging.setMessage(parts[3].substring(parts[3].indexOf(" ") + 1));
                } else if (logging != null) {
                    // This is a continuation of the last log entry's message
                    logging.setMessage(logging.getMessage() + "\n" + line);
                }

                // Check for the "Authenticated User" field
                if (line.contains("Authenticated User: ")) {
                    String user = line.split("Authenticated User: ")[1].trim();
                    logging.setUser(user);
                }
            }

            // Add the last log entry if it exists
            if (logging != null) {
                logEntries.add(logging);
            }
        }

        return logEntries;
    }
}

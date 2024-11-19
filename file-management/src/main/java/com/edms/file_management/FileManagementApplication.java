package com.edms.file_management;

import com.edms.file_management.filemanager.FileManagerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FileManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(FileManagementApplication.class, args);
	}

	@Bean
	CommandLineRunner init(FileManagerService storageService) {
		return (args) -> {
			storageService.init();
		};
	}

}

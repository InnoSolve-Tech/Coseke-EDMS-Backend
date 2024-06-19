package com.cosek.edms;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import com.cosek.edms.filemanager.FileManagerService;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class EdmsApplication {

	public static void main(String[] args) {
		SpringApplication.run(EdmsApplication.class, args);
		
	}

	@Bean
	CommandLineRunner init(FileManagerService storageService) {
		return (args) -> {
			storageService.init();
		};
	}

}

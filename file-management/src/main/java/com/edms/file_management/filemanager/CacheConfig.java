package com.edms.file_management.filemanager;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public ConcurrentHashMap<String, AtomicInteger> folderFileCountCache() {
        return new ConcurrentHashMap<>();
    }
}

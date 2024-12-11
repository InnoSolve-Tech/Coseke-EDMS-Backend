package com.edms.workflows.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@ConfigurationProperties("storage")
public class StorageProperties {

    private String location = System.getenv("STORAGE_LOCATION") != null ?
        System.getenv("STORAGE_LOCATION") : "/app/storage"; // Default to the mapped Docker volume location

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

}

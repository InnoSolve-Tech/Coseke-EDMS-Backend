package com.cosek.edms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.io.File;

@ConfigurationProperties("storage")
public class StorageProperties {

	private String location = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "test";

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
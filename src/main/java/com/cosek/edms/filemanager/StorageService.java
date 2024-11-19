package com.cosek.edms.filemanager;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

	void init() throws Exception;

	void store(FileManager data, MultipartFile file) throws Exception;

	void bulkStore(FileManager[] data, MultipartFile[] files) throws Exception;

	Stream<Path> loadAll(Long folderID) throws Exception;

	Path load(String filename, Long folderID) throws Exception;

	Resource loadAsResource(String filename, Long folderID) throws Exception;

	void deleteAll();

}

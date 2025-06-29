package com.edms.file_management.filemanager;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

	void init() throws Exception;

	FileManager store(FileManager data, MultipartFile file) throws Exception;

	List<FileManager> bulkStore(FileManager[] data, MultipartFile[] files) throws Exception;

	Stream<Path> loadAll(Long folderID) throws Exception;

	Path load(String filename, Long folderID) throws Exception;

	Resource loadAsResource(String filename, Long folderID) throws Exception;

	void deleteAll();

}

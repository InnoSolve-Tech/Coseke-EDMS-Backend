package com.cosek.edms.filemanager;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

	void init() throws Exception;

	void store(String date, MultipartFile file, Long folderID) throws Exception;

	void bulkStore(String[] dates, MultipartFile[] files, Long folderID) throws Exception;

	Stream<Path> loadAll(Long folderID) throws Exception;

	Path load(String filename, Long folderID) throws Exception;

	Resource loadAsResource(String filename, Long folderID) throws Exception;

	void deleteAll();

}

package com.edms.file_management.search;

import com.edms.file_management.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SearchService {
        @Autowired
    private SearchRepository searchRepository;

    public List<Search> searchFiles(String keyword) {
        Optional<List<Search>> optionalFiles = searchRepository.searchFiles(keyword);
        return optionalFiles.orElseThrow(() -> new ResourceNotFoundException("No files found with keyword: " + keyword));

    }
    
}

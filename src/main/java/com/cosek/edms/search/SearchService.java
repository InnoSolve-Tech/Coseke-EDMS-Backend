package com.cosek.edms.search;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cosek.edms.exception.ResourceNotFoundException;

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

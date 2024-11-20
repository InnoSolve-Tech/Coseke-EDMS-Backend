package com.edms.file_management.search;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {
    @Autowired
    private SearchService searchService;

    @GetMapping
    public List<Search> searchFiles(@RequestParam String keyword) {
        return searchService.searchFiles(keyword);
    }
    
}

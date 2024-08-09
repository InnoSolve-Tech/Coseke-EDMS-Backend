package com.cosek.edms.search;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

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

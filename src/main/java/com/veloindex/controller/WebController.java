package com.veloindex.controller;

import com.veloindex.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class WebController {

    private final SearchService searchService;

    public WebController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/search")
    public String search(@RequestParam String query, Model model) {
        List<SearchService.SearchResult> results = searchService.search(query);
        model.addAttribute("results", results);
        model.addAttribute("query", query);
        return "index";
    }
}

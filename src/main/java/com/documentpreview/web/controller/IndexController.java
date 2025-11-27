package com.documentpreview.web.controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class IndexController {
    @GetMapping("/api/health")
    public String health() { return "ok"; }
}

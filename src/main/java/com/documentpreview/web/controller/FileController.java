package com.documentpreview.web.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/v1")
public class FileController {
    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${app.scan.root-dirs}")
    private String rootDir;

    @GetMapping("/fs/**")
    public ResponseEntity<?> serveFile(HttpServletRequest request) {
        try {
            String uri = request.getRequestURI();
            String prefix = "/api/v1/fs/";
            int idx = uri.indexOf(prefix);
            String path = idx >= 0 ? uri.substring(idx + prefix.length()) : "";
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);

            if (path.isEmpty() || path.contains("..")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid path");
            }

            Path root = Paths.get(rootDir).normalize();
            Path target = root.resolve(path).normalize();

            if (!target.startsWith(root) || !Files.exists(target) || !Files.isRegularFile(target)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }

            byte[] bytes = Files.readAllBytes(target);
            String mime = Files.probeContentType(target);
            if (mime == null) {
                mime = "application/octet-stream";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", mime);
            headers.set("Content-Disposition", "inline");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to serve file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
        }
    }
}

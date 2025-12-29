package com.documentpreview.web.controller;

import com.documentpreview.modules.config.service.ConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final ConfigService configService;

    // 构造函数注入ConfigService
    public FileController(ConfigService configService) {
        this.configService = configService;
    }

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

            // 从ConfigService获取当前根目录
            String rootDirs = configService.getRootDirs();
            Path target = null;

            if (rootDirs != null && !rootDirs.isEmpty()) {
                String[] roots = rootDirs.split(",");
                for (String r : roots) {
                    try {
                        Path root = Paths.get(r.trim()).normalize();
                        Path candidate = root.resolve(path).normalize();

                        // 安全检查：确保文件在根目录下，且存在
                        if (candidate.startsWith(root) && Files.exists(candidate) && Files.isRegularFile(candidate)) {
                            target = candidate;
                            break;
                        }
                    } catch (Exception e) {
                        logger.warn("Invalid root path or error resolving file: {}", r, e);
                    }
                }
            }

            if (target == null) {
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

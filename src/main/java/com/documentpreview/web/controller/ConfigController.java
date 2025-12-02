package com.documentpreview.web.controller;

import com.documentpreview.modules.config.service.ConfigService;
import com.documentpreview.modules.scan.service.FileScanServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/config")
public class ConfigController {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);
    
    private final ConfigService configService;
    private final FileScanServiceImpl fileScanService;
    
    // 构造函数注入依赖
    public ConfigController(ConfigService configService, FileScanServiceImpl fileScanService) {
        this.configService = configService;
        this.fileScanService = fileScanService;
    }
    
    // 获取当前根目录配置
    @GetMapping("/root-dirs")
    public ResponseEntity<String> getRootDirs() {
        String rootDirs = configService.getRootDirs();
        logger.debug("Get root dirs: {}", rootDirs);
        return ResponseEntity.ok(rootDirs);
    }
    
    // 更新根目录配置
    @PutMapping("/root-dirs")
    public ResponseEntity<Void> updateRootDirs(@RequestBody String rootDirs) {
        logger.info("Update root dirs from {} to {}", configService.getRootDirs(), rootDirs);
        // 更新配置服务中的根目录
        configService.updateRootDirs(rootDirs);
        // 更新FileScanServiceImpl中的根目录
        fileScanService.updateRootDirs(rootDirs);
        return ResponseEntity.ok().build();
    }
}
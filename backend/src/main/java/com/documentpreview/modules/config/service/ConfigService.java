package com.documentpreview.modules.config.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Configuration service for managing dynamic configuration properties.
 * This service provides a centralized way to access and update configuration values.
 */
@Service
public class ConfigService {
    
    private static final Logger logger = LoggerFactory.getLogger(ConfigService.class);
    
    // 扫描根目录配置
    private String rootDirs;
    
    /**
     * Initialize root directories from application properties.
     * 
     * @param rootDirs The initial root directories from configuration file
     */
    @Value("${app.scan.root-dirs}")
    public void setInitialRootDirs(String rootDirs) {
        this.rootDirs = rootDirs;
        logger.info("Initialized root directories: {}", rootDirs);
    }
    
    /**
     * Get the current root directories for scanning.
     * 
     * @return The current root directories
     */
    public String getRootDirs() {
        return rootDirs;
    }
    
    /**
     * Update the root directories for scanning.
     * 
     * @param newRootDirs The new root directories to use
     */
    public void updateRootDirs(String newRootDirs) {
        logger.info("Updating root directories from {} to {}", this.rootDirs, newRootDirs);
        this.rootDirs = newRootDirs;
    }
}
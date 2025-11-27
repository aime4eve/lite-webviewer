package com.documentpreview.modules.scan.service;

import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.domain.FilesIndexItem;
import com.documentpreview.modules.scan.domain.ScanFinishedEvent;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Implementation of the FileScanService interface.
 * This service handles the actual file system scanning, change detection, and index generation.
 */
@Service
public class FileScanServiceImpl implements FileScanService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileScanServiceImpl.class);
    
    // Blacklist of directories to skip during scanning
    private static final Set<String> BLACKLISTED_DIRS = Set.of(
            ".git", ".svn", "node_modules", "target", ".DS_Store"
    );
    
    // Attributes to follow when visiting files
    private static final LinkOption[] LINK_OPTIONS = { LinkOption.NOFOLLOW_LINKS };
    
    @Value("${app.scan.root-dirs}")
    private String rootDirs;
    
    @Value("${app.data.dir}")
    private String dataDir;
    
    private final ApplicationEventPublisher eventPublisher;
    
    // Thread-safe state management
    private final AtomicBoolean isScanning = new AtomicBoolean(false);
    private volatile FilesIndex lastIndex;
    private volatile String lastScanStatus = "Not started";
    private volatile long lastScanTimestamp = -1;
    
    // Cache for last modified times to detect changes
    private final Map<String, Long> lastModifiedCache = new ConcurrentHashMap<>();
    
    public FileScanServiceImpl(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    @Override
    public Result<FilesIndex> scanRootDirectories() {
        logger.info("Scanning root directory: {}", rootDirs);
        return scanDirectories(Collections.singletonList(rootDirs));
    }
    
    @Override
    public Result<FilesIndex> scanDirectory(String dirPath) {
        Objects.requireNonNull(dirPath, "Directory path cannot be null");
        logger.info("Scanning single directory: {}", dirPath);
        return scanDirectories(Collections.singletonList(dirPath));
    }
    
    @Override
    public Result<FilesIndex> scanDirectories(List<String> dirPaths) {
        Objects.requireNonNull(dirPaths, "Directory paths cannot be null");
        
        if (dirPaths.isEmpty()) {
            return Result.failure("No directories to scan");
        }
        
        // Check if already scanning
        if (!isScanning.compareAndSet(false, true)) {
            return Result.failure("Scan already in progress");
        }
        
        Instant startTime = Instant.now();
        List<FilesIndexItem> allItems = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger fileCount = new AtomicInteger(0);
        AtomicInteger dirCount = new AtomicInteger(0);
        
        try {
            logger.info("Starting scan of {} directories", dirPaths.size());
            lastScanStatus = "Scanning...";
            
            // Use ForkJoinPool for efficient parallel scanning
            ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
            
            for (String dirPath : dirPaths) {
                Path rootPath = Paths.get(dirPath);
                
                // Validate directory exists and is readable
                if (!Files.exists(rootPath) || !Files.isDirectory(rootPath)) {
                    logger.warn("Directory does not exist or is not a directory: {}", dirPath);
                    continue;
                }
                
                // Walk the file tree recursively
                forkJoinPool.submit(() -> {
                    try {
                        Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                            
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                // Check if directory should be skipped
                                if (shouldSkipDirectory(dir)) {
                                    logger.debug("Skipping blacklisted directory: {}", dir);
                                    return FileVisitResult.SKIP_SUBTREE;
                                }
                                
                                // Check symbolic link safety
                                if (Files.isSymbolicLink(dir)) {
                                    if (!isSafeSymbolicLink(dir, rootPath)) {
                                        logger.warn("Skipping unsafe symbolic link: {}", dir);
                                        return FileVisitResult.SKIP_SUBTREE;
                                    }
                                }
                                
                                // Add directory to index
                                String relativePath = rootPath.relativize(dir).toString();
                                if (relativePath.isEmpty()) {
                                    relativePath = dir.getFileName().toString();
                                }
                                
                                // Ensure path ends with slash for directories
                                if (!relativePath.endsWith(".")) {
                                    relativePath = relativePath + "/";
                                }
                                
                                allItems.add(FilesIndexItem.directory(relativePath, dir.getFileName().toString()));
                                dirCount.incrementAndGet();
                                
                                return FileVisitResult.CONTINUE;
                            }
                            
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                // Check if file should be processed
                                if (shouldProcessFile(file)) {
                                    // Check if file has changed
                                    String filePath = file.toString();
                                    long lastModified = attrs.lastModifiedTime().toMillis();
                                    
                                    // Only process if file is new or has changed
                                    if (!lastModifiedCache.containsKey(filePath) || 
                                        lastModifiedCache.get(filePath) != lastModified) {
                                        
                                        // Update cache
                                        lastModifiedCache.put(filePath, lastModified);
                                        
                                        // Add file to index
                                        String relativePath = rootPath.relativize(file).toString();
                                        allItems.add(FilesIndexItem.file(relativePath, file.getFileName().toString()));
                                        fileCount.incrementAndGet();
                                    }
                                }
                                
                                return FileVisitResult.CONTINUE;
                            }
                            
                            @Override
                            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                                logger.error("Failed to visit file: {}, error: {}", file, exc.getMessage());
                                return FileVisitResult.CONTINUE; // Continue with other files
                            }
                        });
                    } catch (IOException e) {
                        logger.error("Error scanning directory: {}, error: {}", rootPath, e.getMessage());
                    }
                }).get(); // Wait for completion
            }
            
            // Create index from collected items
            FilesIndex index = FilesIndex.create(allItems, dirPaths.get(0));
            lastIndex = index;
            lastScanTimestamp = System.currentTimeMillis();
            lastScanStatus = String.format("Completed: %d files, %d directories", 
                    index.getFileCount(), index.getDirectoryCount());
            
            logger.info("Scan completed successfully: {}", lastScanStatus);
            
            // Publish scan finished event
            Instant endTime = Instant.now();
            ScanFinishedEvent event = new ScanFinishedEvent(
                    dirPaths,
                    index.getFileCount(),
                    index.getDirectoryCount(),
                    startTime,
                    endTime
            );
            eventPublisher.publishEvent(event);
            
            return Result.success(index);
            
        } catch (Exception e) {
            String errorMessage = String.format("Scan failed: %s", e.getMessage());
            logger.error(errorMessage, e);
            lastScanStatus = errorMessage;
            return Result.failure(errorMessage);
        } finally {
            isScanning.set(false);
        }
    }
    
    @Override
    public Result<FilesIndex> forceFullScan() {
        logger.info("Forcing full scan (clearing cache)");
        // Clear cache to force full scan
        lastModifiedCache.clear();
        return scanRootDirectories();
    }
    
    @Override
    public Result<FilesIndex> getLastIndex() {
        if (lastIndex == null) {
            return Result.failure("No index available yet");
        }
        return Result.success(lastIndex);
    }
    
    @Override
    public boolean isScanInProgress() {
        return isScanning.get();
    }
    
    @Override
    public String getLastScanStatus() {
        return lastScanStatus;
    }
    
    @Override
    public long getLastScanTimestamp() {
        return lastScanTimestamp;
    }
    
    /**
     * Checks if a directory should be skipped during scanning.
     * 
     * @param dir The directory to check
     * @return true if the directory should be skipped, false otherwise
     */
    private boolean shouldSkipDirectory(Path dir) {
        String dirName = dir.getFileName().toString();
        return BLACKLISTED_DIRS.contains(dirName) || dirName.startsWith(".");
    }
    
    /**
     * Checks if a file should be processed during scanning.
     * 
     * @param file The file to check
     * @return true if the file should be processed, false otherwise
     */
    private boolean shouldProcessFile(Path file) {
        // Only process regular files (not symbolic links)
        try {
            return Files.isRegularFile(file, LINK_OPTIONS);
        } catch (Exception e) {
            logger.error("Error checking file type: {}, error: {}", file, e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a symbolic link is safe to follow.
     * A symbolic link is considered safe if it points to a location within the root directory.
     * 
     * @param link The symbolic link to check
     * @param rootPath The root directory path
     * @return true if the symbolic link is safe, false otherwise
     */
    private boolean isSafeSymbolicLink(Path link, Path rootPath) {
        try {
            Path target = Files.readSymbolicLink(link);
            Path resolved = link.getParent().resolve(target).normalize();
            
            // Check if resolved path starts with root path
            return resolved.startsWith(rootPath);
        } catch (IOException e) {
            logger.error("Error checking symbolic link: {}, error: {}", link, e.getMessage());
            return false;
        }
    }
    
    /**
     * Normalizes a file path to ensure consistent representation.
     * 
     * @param path The path to normalize
     * @return The normalized path
     */
    private String normalizePath(String path) {
        return path.replace("\\", "/");
    }
}
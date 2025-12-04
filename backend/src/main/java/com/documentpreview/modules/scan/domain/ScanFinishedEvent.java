package com.documentpreview.modules.scan.domain;

import com.documentpreview.shared.ddd.DomainEvent;

import java.time.Instant;
import java.util.List;

/**
 * Domain event published when a file scan operation has finished.
 */
public class ScanFinishedEvent extends DomainEvent {
    private final List<String> rootDirs;
    private final int fileCount;
    private final int dirCount;
    private final Instant startTime;
    private final Instant endTime;

    /**
     * Creates a new ScanFinishedEvent instance.
     * 
     * @param rootDirs The root directories that were scanned.
     * @param fileCount The number of files found during the scan.
     * @param dirCount The number of directories found during the scan.
     * @param startTime The time when the scan started.
     * @param endTime The time when the scan finished.
     */
    public ScanFinishedEvent(List<String> rootDirs, int fileCount, int dirCount, Instant startTime, Instant endTime) {
        this.rootDirs = rootDirs;
        this.fileCount = fileCount;
        this.dirCount = dirCount;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Gets the root directories that were scanned.
     * 
     * @return The list of root directories.
     */
    public List<String> getRootDirs() {
        return rootDirs;
    }

    /**
     * Gets the number of files found during the scan.
     * 
     * @return The file count.
     */
    public int getFileCount() {
        return fileCount;
    }

    /**
     * Gets the number of directories found during the scan.
     * 
     * @return The directory count.
     */
    public int getDirCount() {
        return dirCount;
    }

    /**
     * Gets the time when the scan started.
     * 
     * @return The start time.
     */
    public Instant getStartTime() {
        return startTime;
    }

    /**
     * Gets the time when the scan finished.
     * 
     * @return The end time.
     */
    public Instant getEndTime() {
        return endTime;
    }

    /**
     * Gets the duration of the scan in milliseconds.
     * 
     * @return The scan duration in milliseconds.
     */
    public long getDurationMs() {
        return endTime.toEpochMilli() - startTime.toEpochMilli();
    }

    @Override
    public String getEventType() {
        return "ScanFinished";
    }

    @Override
    public String toString() {
        return String.format("ScanFinishedEvent{rootDirs=%s, fileCount=%d, dirCount=%d, durationMs=%d}",
                rootDirs, fileCount, dirCount, getDurationMs());
    }
}
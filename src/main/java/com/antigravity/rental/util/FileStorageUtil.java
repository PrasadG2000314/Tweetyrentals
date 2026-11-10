package com.antigravity.rental.util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

/**
 * ============================================================================
 * UTILITY COMPONENT: Thread-Safe File Storage Engine
 * ============================================================================
 * OOP CONCEPT HIGHLIGHTS:
 * 1. Information Hiding:
 *    - Internal lock mechanisms, file buffers, and temporary swap operations
 *      are kept strictly private.
 * 2. Encapsulation:
 *    - Exposes high-level atomic file CRUD operations (read, append, update, delete)
 *      without leaking low-level OS I/O streams to calling services.
 * 3. Robust Concurrency:
 *    - Uses ReentrantReadWriteLock per file path to prevent race conditions during
 *      concurrent web requests in a multi-threaded web container.
 * ============================================================================
 */
public class FileStorageUtil {

    // Cache of locks per file path to ensure isolated thread safety across different .txt files
    private static final Map<String, ReentrantReadWriteLock> FILE_LOCKS = new ConcurrentHashMap<>();

    // Private constructor to enforce utility pattern (Information Hiding)
    private FileStorageUtil() {
        throw new UnsupportedOperationException("FileStorageUtil is a static utility class and cannot be instantiated.");
    }

    /**
     * Retrieves or creates a dedicated ReadWriteLock for a given file path.
     */
    private static ReentrantReadWriteLock getLock(String filePath) {
        return FILE_LOCKS.computeIfAbsent(filePath, k -> new ReentrantReadWriteLock(true)); // fair lock
    }

    /**
     * Ensures that the target file and its parent directories exist before any read/write.
     */
    public static void ensureFileExists(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (path.getParent() != null && !Files.exists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    /**
     * Reads all non-empty lines from the specified file securely.
     * Uses ReadLock to allow multiple concurrent readers.
     *
     * @param filePath Path to the .txt file
     * @return List of line strings
     */
    public static List<String> readAllLines(String filePath) {
        ReentrantReadWriteLock.ReadLock readLock = getLock(filePath).readLock();
        readLock.lock();
        try {
            ensureFileExists(filePath);
            List<String> lines = new ArrayList<>();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) { // Ignore empty lines and comments
                        lines.add(line);
                    }
                }
            }
            return lines;
        } catch (IOException e) {
            System.err.println("[FileStorageUtil ERROR] Failed to read from " + filePath + ": " + e.getMessage());
            return Collections.emptyList();
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Appends a single record line to the file.
     * Uses WriteLock for exclusive access.
     *
     * @param filePath Path to the .txt file
     * @param recordLine Serialized string representation of the entity
     * @return true if appended successfully, false otherwise
     */
    public static boolean appendLine(String filePath, String recordLine) {
        if (recordLine == null || recordLine.trim().isEmpty()) {
            return false;
        }

        ReentrantReadWriteLock.WriteLock writeLock = getLock(filePath).writeLock();
        writeLock.lock();
        try {
            ensureFileExists(filePath);
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(filePath),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND)) {
                writer.write(recordLine.trim());
                writer.newLine();
                writer.flush();
                return true;
            }
        } catch (IOException e) {
            System.err.println("[FileStorageUtil ERROR] Failed to append to " + filePath + ": " + e.getMessage());
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Overwrites the entire file with the provided list of records.
     * Utilizes an atomic write-to-temp and rename pattern to prevent file corruption
     * if the server crashes mid-write.
     *
     * @param filePath Target file path
     * @param records List of records to write
     * @return true if successfully saved, false otherwise
     */
    public static boolean writeAllLines(String filePath, List<String> records) {
        ReentrantReadWriteLock.WriteLock writeLock = getLock(filePath).writeLock();
        writeLock.lock();
        try {
            ensureFileExists(filePath);
            Path targetPath = Paths.get(filePath);
            Path tempPath = Paths.get(filePath + ".tmp");

            // Write to temporary buffer first
            try (BufferedWriter writer = Files.newBufferedWriter(
                    tempPath,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING)) {
                for (String record : records) {
                    if (record != null && !record.trim().isEmpty()) {
                        writer.write(record.trim());
                        writer.newLine();
                    }
                }
                writer.flush();
            }

            // Atomically replace existing file
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            System.err.println("[FileStorageUtil ERROR] Failed atomic write to " + filePath + ": " + e.getMessage());
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Updates an existing line identified by a matching predicate.
     *
     * @param filePath Target file path
     * @param matcher Predicate to locate the record to update
     * @param newRecord The replacement record string
     * @return true if record was found and replaced, false otherwise
     */
    public static boolean updateLine(String filePath, Predicate<String> matcher, String newRecord) {
        ReentrantReadWriteLock.WriteLock writeLock = getLock(filePath).writeLock();
        writeLock.lock();
        try {
            List<String> lines = readAllLines(filePath);
            boolean updated = false;
            List<String> newLines = new ArrayList<>();

            for (String line : lines) {
                if (!updated && matcher.test(line)) {
                    newLines.add(newRecord);
                    updated = true;
                } else {
                    newLines.add(line);
                }
            }

            if (updated) {
                return writeAllLines(filePath, newLines);
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Deletes any record line matching the predicate.
     *
     * @param filePath Target file path
     * @param matcher Predicate condition for records to delete
     * @return true if at least one matching record was removed
     */
    public static boolean deleteLine(String filePath, Predicate<String> matcher) {
        ReentrantReadWriteLock.WriteLock writeLock = getLock(filePath).writeLock();
        writeLock.lock();
        try {
            List<String> lines = readAllLines(filePath);
            List<String> remaining = new ArrayList<>();
            boolean deleted = false;

            for (String line : lines) {
                if (matcher.test(line)) {
                    deleted = true;
                } else {
                    remaining.add(line);
                }
            }

            if (deleted) {
                return writeAllLines(filePath, remaining);
            }
            return false;
        } finally {
            writeLock.unlock();
        }
    }
}


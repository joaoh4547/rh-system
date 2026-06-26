package com.rhsystem.infrastructure.storage;

import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.port.FileStorage;
import com.rhsystem.infrastructure.config.RhSystemProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.stereotype.Component;

/**
 * Stores attachments on the filesystem and returns the relative path.
 */
@Component
public class LocalFileStorage implements FileStorage {

    private final RhSystemProperties properties;

    public LocalFileStorage(RhSystemProperties properties) {
        this.properties = properties;
    }

    @Override
    public String store(byte[] content, String fileName) {
        try {
            Path dir = Paths.get(properties.getStorageDir());
            Files.createDirectories(dir);
            String uniqueName = UUID.randomUUID() + "_" + sanitize(fileName);
            Path target = dir.resolve(uniqueName);
            Files.write(target, content == null ? new byte[0] : content);
            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("Falha ao armazenar o arquivo: " + fileName);
        }
    }

    private String sanitize(String name) {
        return name == null ? "file" : name.replaceAll("[^A-Za-z0-9._-]", "_");
    }
}

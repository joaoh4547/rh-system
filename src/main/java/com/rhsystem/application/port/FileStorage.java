package com.rhsystem.application.port;

/**
 * Output port for file storage (attachments).
 * Implemented in the infrastructure layer.
 */
public interface FileStorage {

    /**
     * Stores the content and returns the path/identifier for later retrieval.
     */
    String store(byte[] content, String fileName);
}

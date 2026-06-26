package com.rhsystem.application.dto.usuario;

/**
 * Represents an attachment submitted during registration (in-memory content + metadata).
 */
public record DocumentUpload(
        String description,
        String fileName,
        String contentType,
        byte[] content
) {
}

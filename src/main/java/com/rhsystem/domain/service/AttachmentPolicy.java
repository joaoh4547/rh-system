package com.rhsystem.domain.service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Domain service: which user attachments are accepted.
 *
 * <p>Allowlist: PDF, PNG and JPEG, up to {@link #MAX_SIZE_BYTES} each and
 * {@link #MAX_FILES} per user. The type is detected from the file's <b>magic
 * bytes</b> — the name/extension and the MIME type sent by the browser are
 * client-controlled and therefore not trusted (an {@code .html} or {@code .svg}
 * renamed to {@code .pdf} is rejected; served back later it would be stored XSS).</p>
 */
public final class AttachmentPolicy {

    public static final int MAX_SIZE_BYTES = 5 * 1024 * 1024;
    public static final int MAX_FILES = 10;

    private static final byte[] PDF = {'%', 'P', 'D', 'F', '-'};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private AttachmentPolicy() {
    }

    /** MIME type detected from the content, or empty if not an allowed type. */
    public static Optional<String> detectAllowedType(byte[] content) {
        if (startsWith(content, PDF)) {
            return Optional.of("application/pdf");
        }
        if (startsWith(content, PNG)) {
            return Optional.of("image/png");
        }
        if (startsWith(content, JPEG)) {
            return Optional.of("image/jpeg");
        }
        return Optional.empty();
    }

    public static boolean isWithinSizeLimit(byte[] content) {
        return content != null && content.length > 0 && content.length <= MAX_SIZE_BYTES;
    }

    private static boolean startsWith(byte[] content, byte[] magic) {
        return content != null && content.length >= magic.length
                && Arrays.equals(Arrays.copyOf(content, magic.length), magic);
    }
}

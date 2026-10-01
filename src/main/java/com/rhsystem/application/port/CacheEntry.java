package com.rhsystem.application.port;

/**
 * Detail of a single entry stored in a cache region (Hazelcast IMap).
 *
 * @param key          the entry key (composed cache key, e.g. {@code all}, {@code count})
 * @param valueType    simple class name of the cached value
 * @param valuePreview short, human-readable preview of the cached value
 * @param sizeBytes    approximate heap cost of the entry in bytes
 * @param hits         number of times this specific entry was read
 */
public record CacheEntry(
        String key,
        String valueType,
        String valuePreview,
        long sizeBytes,
        long hits
) {}

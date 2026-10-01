package com.rhsystem.application.dto.cache;

/**
 * Read-only snapshot of a distributed cache region.
 *
 * @param name        the cache/region name (e.g. {@code users}, {@code groups})
 * @param entryCount  number of entries currently owned by this member
 * @param memoryBytes approximate heap memory cost of the owned entries, in bytes
 */
public record CacheInfo(String name, long entryCount, long memoryBytes) {
}

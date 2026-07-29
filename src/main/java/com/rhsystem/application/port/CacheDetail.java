package com.rhsystem.application.port;

/**
 * Detailed runtime statistics for a single cache region (Hazelcast IMap).
 *
 * @param name             cache/region name
 * @param entryCount       number of owned entries in this member
 * @param backupEntryCount number of backup entries kept on this member
 * @param memoryBytes      approximate heap cost (owned entries) in bytes
 * @param backupMemoryBytes approximate heap cost of backup entries in bytes
 * @param hits             number of cache hits (get returning a value)
 * @param getCount         total get operations
 */
public record CacheDetail(
        String name,
        long entryCount,
        long backupEntryCount,
        long memoryBytes,
        long backupMemoryBytes,
        long hits,
        long getCount
) {}

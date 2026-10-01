package com.rhsystem.application.port;

import com.rhsystem.application.dto.cache.CacheInfo;

import java.util.List;

/**
 * Output port for administering the distributed cache regions.
 *
 * <p>Implemented by the infrastructure layer over the underlying cache provider
 * (Hazelcast). Allows querying cache usage (entry counts / memory footprint) and
 * evicting a single region or every region at once.</p>
 */
public interface CacheAdministration {

    /**
     * Lists every managed cache region with its current usage snapshot.
     */
    List<CacheInfo> listCaches();

    /**
     * Evicts all entries of the given cache region.
     *
     * @param cacheName the region name to clear
     */
    void clear(String cacheName);

    /**
     * Evicts all entries of every managed cache region.
     */
    void clearAll();
}

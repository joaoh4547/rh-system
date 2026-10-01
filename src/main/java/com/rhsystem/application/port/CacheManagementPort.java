package com.rhsystem.application.port;

import java.util.List;
import java.util.Map;

public interface CacheManagementPort {
    Map<String, Long> getCacheStats();
    List<CacheDetail> getCacheDetails();
    List<CacheEntry> getCacheEntries(String cacheName);
    void clearCache(String cacheName);
    void clearAllCaches();
}

package com.rhsystem.application.port;

import java.util.Map;

public interface CacheManagementPort {
    Map<String, Long> getCacheStats();
    void clearCache(String cacheName);
    void clearAllCaches();
}

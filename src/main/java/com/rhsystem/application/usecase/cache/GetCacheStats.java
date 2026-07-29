package com.rhsystem.application.usecase.cache;

import com.rhsystem.application.port.CacheDetail;
import com.rhsystem.application.port.CacheEntry;
import com.rhsystem.application.port.CacheManagementPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class GetCacheStats {

    private final CacheManagementPort cacheManagement;

    public GetCacheStats(CacheManagementPort cacheManagement) {
        this.cacheManagement = cacheManagement;
    }

    public Map<String, Long> execute() {
        return cacheManagement.getCacheStats();
    }

    public List<CacheDetail> executeDetails() {
        return cacheManagement.getCacheDetails();
    }

    public List<CacheEntry> executeEntries(String cacheName) {
        return cacheManagement.getCacheEntries(cacheName);
    }
}

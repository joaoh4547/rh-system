package com.rhsystem.application.usecase.cache;

import com.rhsystem.application.port.CacheManagementPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}

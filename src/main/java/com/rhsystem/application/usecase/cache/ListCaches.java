package com.rhsystem.application.usecase.cache;

import com.rhsystem.application.dto.cache.CacheInfo;
import com.rhsystem.application.port.CacheAdministration;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Lists every managed cache region with its current usage snapshot
 * (entry count and memory footprint).
 */
@Service
@AllArgsConstructor
public class ListCaches {

    private final CacheAdministration cacheAdministration;

    public List<CacheInfo> execute() {
        return cacheAdministration.listCaches();
    }
}

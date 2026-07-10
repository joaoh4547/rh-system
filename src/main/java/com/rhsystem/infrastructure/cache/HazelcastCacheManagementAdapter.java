package com.rhsystem.infrastructure.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.rhsystem.application.port.CacheManagementPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class HazelcastCacheManagementAdapter implements CacheManagementPort {

    private final HazelcastInstance hazelcastInstance;

    public HazelcastCacheManagementAdapter(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public Map<String, Long> getCacheStats() {
        Map<String, Long> stats = new HashMap<>();
        hazelcastInstance.getDistributedObjects().stream()
                .filter(obj -> obj instanceof IMap)
                .forEach(obj -> {
                    IMap<?, ?> map = (IMap<?, ?>) obj;
                    stats.put(map.getName(), (long) map.size());
                });
        return stats;
    }

    @Override
    public void clearCache(String cacheName) {
        hazelcastInstance.getMap(cacheName).clear();
    }

    @Override
    public void clearAllCaches() {
        hazelcastInstance.getDistributedObjects().stream()
                .filter(obj -> obj instanceof IMap)
                .forEach(obj -> ((IMap<?, ?>) obj).clear());
    }
}

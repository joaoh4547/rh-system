package com.rhsystem.infrastructure.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.map.LocalMapStats;
import com.hazelcast.core.EntryView;
import com.rhsystem.application.port.CacheDetail;
import com.rhsystem.application.port.CacheEntry;
import com.rhsystem.application.port.CacheManagementPort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
    public List<CacheDetail> getCacheDetails() {
        List<CacheDetail> details = new ArrayList<>();
        hazelcastInstance.getDistributedObjects().stream()
                .filter(obj -> obj instanceof IMap)
                .forEach(obj -> {
                    IMap<?, ?> map = (IMap<?, ?>) obj;
                    LocalMapStats stats = map.getLocalMapStats();
                    details.add(new CacheDetail(
                            map.getName(),
                            stats.getOwnedEntryCount(),
                            stats.getBackupEntryCount(),
                            stats.getOwnedEntryMemoryCost(),
                            stats.getBackupEntryMemoryCost(),
                            stats.getHits(),
                            stats.getGetOperationCount()));
                });
        details.sort(Comparator.comparing(CacheDetail::name));
        return details;
    }

    @Override
    public List<CacheEntry> getCacheEntries(String cacheName) {
        List<CacheEntry> entries = new ArrayList<>();
        IMap<Object, Object> map = hazelcastInstance.getMap(cacheName);
        for (Object key : map.keySet()) {
            EntryView<Object, Object> view = map.getEntryView(key);
            Object value = view != null ? view.getValue() : map.get(key);
            entries.add(new CacheEntry(
                    String.valueOf(key),
                    value != null ? value.getClass().getSimpleName() : "null",
                    preview(value),
                    view != null ? view.getCost() : 0L,
                    view != null ? view.getHits() : 0L));
        }
        entries.sort(Comparator.comparing(CacheEntry::key));
        return entries;
    }

    private static String preview(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof java.util.Collection<?> collection) {
            return collection.size() + " item(ns)";
        }
        String text = String.valueOf(value);
        return text.length() > 120 ? text.substring(0, 117) + "..." : text;
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

package com.rhsystem.infrastructure.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.rhsystem.application.dto.cache.CacheInfo;
import com.rhsystem.application.port.CacheAdministration;
import com.rhsystem.utils.CacheEntity;
import com.rhsystem.utils.Reflections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@link CacheAdministration} implementation backed by the embedded Hazelcast
 * instance.
 *
 * <p>The managed regions are discovered from the classes annotated with
 * {@link CacheEntity} (the same mechanism {@code CacheConfig} uses to configure
 * the maps), so the screen automatically reflects any new cached aggregate.</p>
 *
 * <p>The {@link HazelcastInstance} is resolved lazily through an
 * {@link ObjectProvider}: when the cache is disabled (e.g. the test profile,
 * {@code rh-system.cache.enabled=false}) no Hazelcast node exists, and the port
 * gracefully degrades to an empty listing / no-op eviction instead of failing
 * the application context.</p>
 */
@Service
@Slf4j
public class HazelcastCacheAdministration implements CacheAdministration {

    private final ObjectProvider<HazelcastInstance> hazelcastInstance;

    public HazelcastCacheAdministration(ObjectProvider<HazelcastInstance> hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @Override
    public List<CacheInfo> listCaches() {
        HazelcastInstance instance = hazelcastInstance.getIfAvailable();
        if (instance == null) {
            return List.of();
        }
        return cacheNames().stream()
                .map(name -> toCacheInfo(instance, name))
                .toList();
    }

    @Override
    public void clear(String cacheName) {
        HazelcastInstance instance = hazelcastInstance.getIfAvailable();
        if (instance == null || cacheName == null || cacheName.isBlank()) {
            return;
        }
        instance.getMap(cacheName).clear();
        log.info("Cache '{}' cleared", cacheName);
    }

    @Override
    public void clearAll() {
        HazelcastInstance instance = hazelcastInstance.getIfAvailable();
        if (instance == null) {
            return;
        }
        cacheNames().forEach(name -> instance.getMap(name).clear());
        log.info("All caches cleared");
    }

    private CacheInfo toCacheInfo(HazelcastInstance instance, String name) {
        IMap<Object, Object> map = instance.getMap(name);
        var stats = map.getLocalMapStats();
        return new CacheInfo(name, stats.getOwnedEntryCount(), stats.getOwnedEntryMemoryCost());
    }

    /**
     * Resolves the managed cache region names from the {@link CacheEntity}
     * annotations, sorted alphabetically for a stable UI ordering.
     */
    private Set<String> cacheNames() {
        Set<String> names = new TreeSet<>();
        for (Class<?> clazz : Reflections.getClassesWithAnnotation(CacheEntity.class)) {
            CacheEntity annotation = Reflections.getAnnotation(CacheEntity.class, clazz);
            if (annotation != null) {
                names.add(annotation.cacheName());
            }
        }
        return names;
    }
}

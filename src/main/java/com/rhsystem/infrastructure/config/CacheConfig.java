package com.rhsystem.infrastructure.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import com.rhsystem.utils.CacheEntity;
import com.rhsystem.utils.Reflections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

/**
 * Distributed cache configuration (Hazelcast embedded).
 *
 * <p>Each application instance embeds a Hazelcast member. Members with the same
 * cluster name discover each other and share the cache maps, so a write (and its
 * eviction) on one instance is immediately visible to all others — no external
 * cache server required.</p>
 *
 * <p>Discovery: if {@code rh-system.cache.members} is set (comma-separated
 * host[:port] list, env {@code HZ_MEMBERS}), TCP-IP discovery is used — the right
 * choice for containers/cloud. When empty, multicast discovery is used, which is
 * enough for instances on the same machine/local network.</p>
 *
 * <p>The whole cache can be turned off with {@code rh-system.cache.enabled=false}
 * (used by the test profile: no Hazelcast node, and without {@code @EnableCaching}
 * the {@code @Cacheable}/{@code @CacheEvict} annotations become no-ops).</p>
 */
@Configuration
@EnableCaching
@ConditionalOnBooleanProperty(name = "rh-system.cache.enabled", matchIfMissing = true)
@Slf4j
public class CacheConfig {

    /**
     * Cache for User aggregate queries.
     */
    public static final String USERS = "users";

    /**
     * Cache for Group aggregate queries.
     */
    public static final String GROUPS = "groups";

    /**
     * Cache for Parameter aggregate queries.
     */
    public static final String PARAMETERS = "parameters";

    @Value("${rh-system.cache.debug-resolve-groups}")
    private boolean debugResolveGroups;


    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance(RhSystemProperties properties) {
        log.info("Configuring Hazelcast instance");
        RhSystemProperties.Cache props = properties.getCache();

        Config config = new Config();
        config.setClusterName(props.getClusterName());
        config.setClassLoader(getClass().getClassLoader());

        config.getNetworkConfig()
                .setPort(props.getPort())
                .setPortAutoIncrement(true);

        configureDiscovery(config.getNetworkConfig().getJoin(), props.getMembers());


        getCacheable().forEach(clazz -> {
            CacheEntity cacheable = Reflections.getAnnotation(CacheEntity.class, clazz);
            var group = cacheable.cacheName();
            if (debugResolveGroups) {
                log.info("Configuring cache for {} using group {}", clazz, group);
            }
            config.addMapConfig(mapConfig(group, props));
        });

        return Hazelcast.newHazelcastInstance(config);
    }

    private Set<Class<?>> getCacheable() {
        return Reflections.getClassesWithAnnotation(CacheEntity.class);
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
        return new HazelcastCacheManager(hazelcastInstance);
    }

    private void configureDiscovery(JoinConfig join, String members) {
        if (members != null && !members.isBlank()) {
            join.getMulticastConfig().setEnabled(false);
            var tcpIp = join.getTcpIpConfig().setEnabled(true);
            for (String member : members.split(",")) {
                tcpIp.addMember(member.trim());
            }
        } else {
            join.getMulticastConfig().setEnabled(true);
        }
    }

    private MapConfig mapConfig(String name, RhSystemProperties.Cache props) {
        return new MapConfig(name)
                .setTimeToLiveSeconds(props.getTtlSeconds())
                .setBackupCount(1)
                .setEvictionConfig(new EvictionConfig()
                        .setEvictionPolicy(EvictionPolicy.LRU)
                        .setMaxSizePolicy(MaxSizePolicy.PER_NODE)
                        .setSize(props.getMaxSize()));
    }
}

package com.rhsystem.infrastructure.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Cache for User aggregate queries. */
    public static final String USERS = "users";

    /** Cache for Group aggregate queries. */
    public static final String GROUPS = "groups";

    @Bean(destroyMethod = "shutdown")
    public HazelcastInstance hazelcastInstance(RhSystemProperties properties) {
        RhSystemProperties.Cache props = properties.getCache();

        Config config = new Config();
        config.setClusterName(props.getClusterName());
        // Devtools/hot-restart friendliness: deserialize with the app classloader.
        config.setClassLoader(getClass().getClassLoader());

        config.getNetworkConfig()
                .setPort(props.getPort())
                .setPortAutoIncrement(true)
                ;

        configureDiscovery(config.getNetworkConfig().getJoin(), props.getMembers());

        config.addMapConfig(mapConfig(USERS, props));
        config.addMapConfig(mapConfig(GROUPS, props));

        return Hazelcast.newHazelcastInstance(config);
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

package com.rhsystem.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application configuration properties (prefix "rh-system").
 */
@Component
@ConfigurationProperties(prefix = "rh-system")
@Getter
@Setter
public class RhSystemProperties {

    /** Base URL for building the activation link. */
    private String baseUrl = "http://localhost:8080";

    /** Email sender address. */
    private String mailFrom = "no-reply@rhsystem.com";

    /** Activation token validity in hours. */
    private long activationTokenValidityHours = 24;

    /** Storage directory for attachments. */
    private String storageDir = "./storage/documentos";

    /** Distributed cache (Hazelcast) settings. */
    private final Cache cache = new Cache();

    /** User session settings (inactivity timer shown in the footer). */
    private final Session session = new Session();

    private final Aes aes = new Aes();

    @Getter
    @Setter
    public static class Aes {
        private String key;
    }

    @Getter
    @Setter
    public static class Session {

        /** Session lifetime without user activity, in minutes. */
        private int timeoutMinutes = 60;

        /** How many minutes before expiration the warning dialog is shown. */
        private int warningMinutes = 5;
    }

    @Getter
    @Setter
    public static class Cache {

        /**
         * Enables the distributed cache (Hazelcast). When {@code false}, no Hazelcast
         * node is started and caching annotations are no-ops (used by the test profile).
         */
        private boolean enabled = true;

        /** Hazelcast cluster name — instances with the same name form a cluster. */
        private String clusterName = "rh-system";

        /**
         * Comma-separated list of known member addresses (host or host:port) for TCP-IP discovery.
         * When empty, multicast discovery is used (works on a local network / same host).
         */
        private String members = "";

        /** Base port for cluster communication (auto-increments if busy). */
        private int port = 5701;

        /** Time-to-live of cached entries, in seconds. */
        private int ttlSeconds = 600;

        /** Maximum entries per cache map, per node (LRU eviction beyond that). */
        private int maxSize = 5000;

        private boolean debugResolveGroups = false;
    }
}

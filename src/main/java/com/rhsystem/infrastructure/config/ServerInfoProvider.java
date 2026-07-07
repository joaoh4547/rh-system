package com.rhsystem.infrastructure.config;

import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Resolves the identity (IP address and hostname) of the running instance.
 *
 * <p>Displayed in the application footer so operators can tell which node
 * (e.g. {@code app1} / {@code app2} behind the load balancer) served a
 * request when investigating logs.</p>
 */
@Component
public class ServerInfoProvider {

    private final String serverAddress;

    public ServerInfoProvider() {
        this.serverAddress = resolveAddress();
    }

    private static String resolveAddress() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            String host = local.getHostName();
            String ip = local.getHostAddress();
            return (host == null || host.isBlank() || host.equals(ip))
                    ? ip
                    : ip + " (" + host + ")";
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }

    /** Server IP, optionally with hostname — e.g. {@code 172.18.0.3 (app1)}. */
    public String getServerAddress() {
        return serverAddress;
    }
}

package com.rhsystem.infrastructure.config;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Logs the effective Vaadin session configuration once at startup.
 *
 * <p>Purpose: {@code closeIdleSessions} and {@code heartbeatInterval} are Vaadin
 * servlet init parameters (not bound to {@code VaadinConfigurationProperties}),
 * so the IDE cannot resolve them and shows a cosmetic warning. This listener
 * prints the values actually applied, confirming the {@code application.yml}
 * settings took effect.</p>
 */
@Slf4j
@Component
public class SessionConfigLogger implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        var config = event.getSource().getDeploymentConfiguration();
        log.info("Vaadin session config -> closeIdleSessions={}, heartbeatInterval={}s",
                config.isCloseIdleSessions(), config.getHeartbeatInterval());
    }
}

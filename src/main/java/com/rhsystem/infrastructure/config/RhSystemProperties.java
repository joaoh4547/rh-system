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
}

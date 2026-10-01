package com.rhsystem.infrastructure.config;

import com.rhsystem.interfaces.ui.pages.auth.LoginView;
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

/**
 * Configuração do Spring Security integrada ao Vaadin (Vaadin 25).
 * A tela de login é registrada via VaadinSecurityConfigurer; as telas públicas
 * (login e ativação) usam @AnonymousAllowed.
 */
@Configuration
@EnableWebSecurity
// @PreAuthorize on use cases = defense in depth below the UI. JSR-250 is deliberately NOT
// enabled: views use @RolesAllowed for Vaadin's own access checker, and with jsr250Enabled
// Spring would wrap those view classes in AOP proxies, which breaks Vaadin components.
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Spring Security already sends X-Content-Type-Options, X-Frame-Options (DENY),
        // Cache-Control and HSTS (on HTTPS). Extra hardening headers:
        http.headers(headers -> headers
                .referrerPolicy(referrer -> referrer.policy(
                        ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                .permissionsPolicyHeader(permissions -> permissions.policy(
                        "camera=(), microphone=(), geolocation=(), payment=(), usb=()")));
        return http.with(VaadinSecurityConfigurer.vaadin(), configurer ->
                configurer.loginView(LoginView.class)).build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

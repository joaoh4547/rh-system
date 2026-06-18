package com.rhsystem;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação RH System.
 */
@SpringBootApplication
@Theme("rh-system")

public class RhSystemApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(RhSystemApplication.class, args);
    }
}

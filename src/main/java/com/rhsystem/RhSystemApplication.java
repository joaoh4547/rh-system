package com.rhsystem;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação RH System.
 */
@SpringBootApplication
@Theme("rh-system")
// Vaadin 25: os módulos Lumo (typography, color, spacing, badge) carregam
// automaticamente; apenas as classes utilitárias (LumoUtility.*) precisam ser
// declaradas explicitamente aqui, substituindo o antigo "utility" do theme.json.
@StyleSheet(Lumo.UTILITY_STYLESHEET)
public class RhSystemApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(RhSystemApplication.class, args);
    }
}

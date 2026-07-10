package com.rhsystem.interfaces.ui;

import com.rhsystem.application.usecase.usuario.GetUserByUserName;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.infrastructure.config.RhSystemProperties;
import com.rhsystem.infrastructure.config.ServerInfoProvider;
import com.rhsystem.interfaces.ui.component.AppFooter;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.component.SessionTimer;
import com.rhsystem.interfaces.ui.pages.groups.GroupPage;
import com.rhsystem.interfaces.ui.pages.parameters.ParameterPage;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.rhsystem.interfaces.ui.pages.editor.RichTextEditorDemoPage;
import com.rhsystem.interfaces.ui.pages.usuario.UserPage;

import jakarta.annotation.security.PermitAll;

/**
 * Layout principal no estilo AdminLTE: sidebar escura com brand,
 * navbar clara com usuário/logout e menu de navegação.
 */
@PermitAll
public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;
    private final GetUserByUserName getUserByUserName;
    private final ServerInfoProvider serverInfoProvider;
    private final RhSystemProperties properties;

    public MainLayout(AuthenticationContext authContext,
                      GetUserByUserName getUserByUserName,
                      ServerInfoProvider serverInfoProvider,
                      RhSystemProperties properties) {
        this.authContext = authContext;
        this.getUserByUserName = getUserByUserName;
        this.serverInfoProvider = serverInfoProvider;
        this.properties = properties;
        setPrimarySection(Section.DRAWER);
        addHeaderContent();
        addDrawerContent();
    }

    private void addHeaderContent() {
        DrawerToggle toggle = new DrawerToggle();
        toggle.setAriaLabel("Menu");

        Span pageTitle = new Span(getTranslation("nav.dashboard"));
        pageTitle.addClassName("nav-page-title");

        String username = authContext.getPrincipalName().orElse(getTranslation("nav.user.fallback"));
        Span user = new Span(VaadinIcon.USER.create(), new Span(" " + username));
        user.addClassName("nav-user");

        Button logout = new Button(getTranslation("action.logout"), VaadinIcon.SIGN_OUT.create(), e -> authContext.logout());
        logout.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout right = new HorizontalLayout(user, logout);
        right.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(toggle, pageTitle, right);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.expand(pageTitle);
        header.setWidthFull();
        header.addClassName("app-navbar");

        addToNavbar(header);
    }

    private void addDrawerContent() {
        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("app-brand");

        String username = authContext.getPrincipalName().orElse(getTranslation("nav.user.fallback"));
        Avatar avatar = new Avatar(username);


        var user = loadUser();

        avatar.addClassName("user-avatar");
        Span nome = new Span(user.getFullName());
        nome.addClassName("user-name");

        Div infos = new Div(nome);
        infos.addClassName("user-infos");
        Div userPanel = new Div(avatar, infos);
        userPanel.addClassName("user-panel");

        Span caption = new Span(getTranslation("nav.section.main"));
        caption.addClassName("nav-caption");

        SideNav nav = new SideNav();
        nav.addClassName("app-nav");


        SideNavItem config = new SideNavItem(getTranslation("nav.section.settings"));
        config.setPrefixComponent(LucideIcon.config());
        config.addItem(new SideNavItem(getTranslation("nav.menu.parameters"), ParameterPage.class, LucideIcon.parameters()));

        SideNavItem security = new SideNavItem(getTranslation("nav.section.security"));
        security.setPrefixComponent(LucideIcon.security());
        security.addItem(new SideNavItem(getTranslation("nav.menu.groups"), GroupPage.class, LucideIcon.users()));
        security.addItem(new SideNavItem(getTranslation("nav.menu.users"), UserPage.class, LucideIcon.user()));

        nav.addItem(config, security);

        RhSystemProperties.Session session = properties.getSession();
        SessionTimer sessionTimer = new SessionTimer(
                session.getTimeoutMinutes(),
                session.getWarningMinutes(),
                authContext::logout);
        AppFooter footer = new AppFooter(serverInfoProvider.getServerAddress(), sessionTimer);
        addToNavbar(footer);

        addToDrawer(brand, userPanel, caption, nav);
    }

    private User loadUser() {
        String username = authContext.getPrincipalName().orElse(null);
        return getUserByUserName.execute(username);
    }
}
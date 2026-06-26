package com.rhsystem.interfaces.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.spring.security.AuthenticationContext;
import com.rhsystem.interfaces.ui.editor.RichTextEditorDemoPage;
import com.rhsystem.interfaces.ui.usuario.UserPage;

import jakarta.annotation.security.PermitAll;

/**
 * Layout principal no estilo AdminLTE: sidebar escura com brand,
 * navbar clara com usuário/logout e menu de navegação.
 */
@PermitAll
public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext;

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
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
        com.vaadin.flow.component.avatar.Avatar avatar =
                new com.vaadin.flow.component.avatar.Avatar(username);
        avatar.addClassName("user-avatar");
        Span nome = new Span(username);
        nome.addClassName("user-name");
        Span papel = new Span(getTranslation("nav.user.role.admin"));
        papel.addClassName("user-role");
        com.vaadin.flow.component.html.Div infos =
                new com.vaadin.flow.component.html.Div(nome, papel);
        infos.addClassName("user-infos");
        com.vaadin.flow.component.html.Div userPanel =
                new com.vaadin.flow.component.html.Div(avatar, infos);
        userPanel.addClassName("user-panel");

        Span caption = new Span(getTranslation("nav.section.main"));
        caption.addClassName("nav-caption");

        SideNav nav = new SideNav();
        nav.addClassName("app-nav");

        // Grupo: Recursos Humanos
        SideNavItem rh = new SideNavItem(getTranslation("nav.section.hr"));
        rh.setPrefixComponent(VaadinIcon.USERS.create());
        rh.addItem(new SideNavItem(getTranslation("nav.menu.users"), UserPage.class, VaadinIcon.USER.create()));
        rh.setExpanded(true);

        // Grupo: Ferramentas
        SideNavItem tools = new SideNavItem("Ferramentas");
        tools.setPrefixComponent(VaadinIcon.TOOLS.create());
        tools.addItem(new SideNavItem("Editor de Texto", RichTextEditorDemoPage.class, VaadinIcon.EDIT.create()));

        // Grupo: Configurações (itens de exemplo, sem rota ainda)
        SideNavItem config = new SideNavItem(getTranslation("nav.section.settings"));
        config.setPrefixComponent(VaadinIcon.COG.create());
        config.addItem(new SideNavItem(getTranslation("nav.menu.profiles")));
        config.addItem(new SideNavItem(getTranslation("nav.menu.parameters")));

        nav.addItem(rh, tools, config);

        addToDrawer(brand, userPanel, caption, nav);
    }
}
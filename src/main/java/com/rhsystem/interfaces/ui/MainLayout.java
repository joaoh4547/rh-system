package com.rhsystem.interfaces.ui;

import com.rhsystem.application.usecase.usuario.GetUserByUserName;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.interfaces.ui.pages.groups.GroupPage;
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

    public MainLayout(AuthenticationContext authContext, GetUserByUserName getUserByUserName) {
        this.authContext = authContext;
        this.getUserByUserName = getUserByUserName;
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

        // Grupo: Ferramentas
        SideNavItem tools = new SideNavItem("Ferramentas");
        tools.setPrefixComponent(VaadinIcon.TOOLS.create());
        tools.addItem(new SideNavItem("Editor de Texto", RichTextEditorDemoPage.class, VaadinIcon.EDIT.create()));

        // Grupo: Configurações (itens de exemplo, sem rota ainda)
        SideNavItem config = new SideNavItem(getTranslation("nav.section.settings"));
        config.setPrefixComponent(VaadinIcon.COG.create());
        config.addItem(new SideNavItem(getTranslation("nav.menu.profiles")));
        config.addItem(new SideNavItem(getTranslation("nav.menu.parameters")));

        SideNavItem security = new SideNavItem(getTranslation("nav.section.security"));
        security.setPrefixComponent(VaadinIcon.LOCK.create());
        security.addItem(new SideNavItem(getTranslation("nav.menu.groups"), GroupPage.class, VaadinIcon.GROUP.create()));
        security.addItem(new SideNavItem(getTranslation("nav.menu.users"), UserPage.class, VaadinIcon.USER.create()));

        nav.addItem(tools, config, security);

        addToDrawer(brand, userPanel, caption, nav);
    }

    private User loadUser(){
        String username = authContext.getPrincipalName().orElse(null);
        return getUserByUserName.execute(username);
    }
}
package com.rhsystem.interfaces.ui.pages.home;

import com.rhsystem.application.usecase.usuario.GetUser;
import com.rhsystem.application.usecase.usuario.GetUserByUserName;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.pages.editor.RichTextEditorDemoPage;
import com.rhsystem.interfaces.ui.pages.groups.GroupPage;
import com.rhsystem.interfaces.ui.pages.usuario.UserPage;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.security.AuthenticationContext;

import jakarta.annotation.security.PermitAll;

/**
 * Home page shown to authenticated users (inside {@link MainLayout}).
 *
 * <p>Intencionalmente <b>não</b> exibe estatísticas da organização (contagens de
 * usuários, grupos, acessos, etc.). Mostra apenas uma saudação ao usuário logado
 * e cartões de acesso rápido às áreas do sistema.</p>
 */
@Route(value = "home", layout = MainLayout.class)
@PageTitle("Início")
@PermitAll
public class HomePage extends VerticalLayout {

    private final AuthenticationContext authContext;
    private final GetUserByUserName getUser;

    public HomePage(AuthenticationContext authContext, GetUserByUserName getUser) {
        this.authContext = authContext;
        this.getUser = getUser;

        addClassName("view");
        setPadding(true);
        setSpacing(true);
        setSizeFull();

        add(buildHero(), buildQuickActions());
    }

    // ── Hero de boas-vindas ────────────────────────────────────────────────────
    private Component buildHero() {
        String username = loadUser().getFullName();

        Span kicker = new Span("RH SYSTEM");
        kicker.addClassName("home-hero-kicker");

        H2 title = new H2("Olá, " + username);
        title.addClassName("home-hero-title");

        Span subtitle = new Span(
                "Bem-vindo ao painel de gestão de recursos humanos. Escolha uma área abaixo para começar.");
        subtitle.addClassName("home-hero-subtitle");

        Div texts = new Div(kicker, title, subtitle);
        texts.addClassName("home-hero-texts");

        Icon icon = VaadinIcon.CUBES.create();
        icon.addClassName("home-hero-icon");

        Div hero = new Div(texts, icon);
        hero.addClassName("home-hero");
        return hero;
    }

    private User loadUser() {
        return getUser.execute(authContext.getPrincipalName().orElseThrow());
    }

    // ── Acesso rápido ──────────────────────────────────────────────────────────
    private Component buildQuickActions() {
        Span title = new Span("Acesso rápido");
        title.addClassName("home-section-title");

        Div grid = new Div();
        grid.addClassName("home-actions");
        grid.add(
                actionCard(LucideIcon.userSquare(), "Usuários",
                        "Cadastre e gerencie os usuários do sistema.", UserPage.class),
                actionCard(LucideIcon.users(), "Grupos",
                        "Defina grupos, permissões e acessos.", GroupPage.class)
                );

        Div section = new Div(title, grid);
        section.addClassName("home-section");
        return section;
    }

    private Component actionCard(Component icon, String title, String description,
                                 Class<? extends Component> target) {

        icon.addClassName("home-action-icon");

        Span t = new Span(title);
        t.addClassName("home-action-title");
        Span d = new Span(description);
        d.addClassName("home-action-desc");

        Div texts = new Div(t, d);
        texts.addClassName("home-action-texts");

        Icon arrow = VaadinIcon.ARROW_RIGHT.create();
        arrow.addClassName("home-action-arrow");

        Div card = new Div(icon, texts, arrow);
        card.addClassName("home-action");
        card.addClickListener(e -> card.getUI().ifPresent(ui -> ui.navigate(target)));
        return card;
    }
}

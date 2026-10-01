package com.rhsystem.interfaces.ui.pages.auth;

import com.rhsystem.application.usecase.usuario.AcceptTerms;
import com.rhsystem.interfaces.ui.security.TermsAcceptanceGuard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.security.AuthenticationContext;
import jakarta.annotation.security.PermitAll;

/**
 * Terms of use, shown <b>after</b> login while the user has not accepted them
 * ({@link TermsAcceptanceGuard} reroutes every navigation here until then).
 *
 * <p>The username comes from the security context — never from the client — so a
 * user can only accept the terms for themselves. Declining logs the user out.</p>
 */
@Route("terms")
@PageTitle("Termos de uso - RH System")
@PermitAll
public class TermsView extends VerticalLayout {

    public TermsView(AcceptTerms acceptTerms, AuthenticationContext authContext) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        H2 title = new H2(getTranslation("login.terms.title"));

        Div content = new Div();
        content.getStyle().set("max-height", "320px").set("overflow", "auto")
                .set("line-height", "1.55").set("color", "var(--lumo-secondary-text-color)");
        content.add(new Paragraph("To access RH System you must read and accept the Terms of Use "
                + "and Privacy Policy."));
        content.add(new Paragraph("By accepting, you agree to the processing of your personal data "
                + "strictly for human-resources management purposes, in compliance with applicable "
                + "data-protection law. Your data will not be shared with third parties without a "
                + "legal basis."));
        content.add(new Paragraph("You may request review or deletion of your data from system "
                + "administrators at any time."));

        Button decline = new Button(getTranslation("login.terms.decline"), e -> authContext.logout());
        decline.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Button accept = new Button(getTranslation("login.terms.accept"), e -> {
            String username = authContext.getPrincipalName().orElseThrow();
            acceptTerms.execute(username);
            TermsAcceptanceGuard.markAccepted(VaadinSession.getCurrent());
            getUI().ifPresent(ui -> ui.navigate(""));
        });
        accept.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout buttons = new HorizontalLayout(decline, accept);

        Div card = new Div(title, content, buttons);
        card.addClassName("login-box");
        card.setMaxWidth("560px");
        add(card);
    }
}

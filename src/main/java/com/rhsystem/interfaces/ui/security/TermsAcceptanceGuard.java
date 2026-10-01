package com.rhsystem.interfaces.ui.security;

import com.rhsystem.application.usecase.usuario.GetUserByUserName;
import com.rhsystem.interfaces.ui.pages.auth.TermsView;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.security.AuthenticationContext;

/**
 * Blocks every route for an authenticated user who has not accepted the terms of
 * use yet, rerouting to {@link TermsView}.
 *
 * <p>Runs as a global {@code BeforeEnterListener} on every UI, so it covers direct
 * URL access too (not only the menu). The database is checked once per session:
 * after a positive answer a flag is kept in the {@link VaadinSession}.</p>
 */
@SpringComponent
public class TermsAcceptanceGuard implements VaadinServiceInitListener {

    private static final String ACCEPTED_ATTRIBUTE = TermsAcceptanceGuard.class.getName() + ".accepted";

    private final AuthenticationContext authContext;
    private final GetUserByUserName getUserByUserName;

    public TermsAcceptanceGuard(AuthenticationContext authContext, GetUserByUserName getUserByUserName) {
        this.authContext = authContext;
        this.getUserByUserName = getUserByUserName;
    }

    /** Called by {@link TermsView} right after the user accepts. */
    public static void markAccepted(VaadinSession session) {
        session.setAttribute(ACCEPTED_ATTRIBUTE, Boolean.TRUE);
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent ->
                uiEvent.getUI().addBeforeEnterListener(this::check));
    }

    private void check(BeforeEnterEvent event) {
        if (event.getNavigationTarget() == TermsView.class || !authContext.isAuthenticated()) {
            return;
        }
        VaadinSession session = event.getUI().getSession();
        if (Boolean.TRUE.equals(session.getAttribute(ACCEPTED_ATTRIBUTE))) {
            return;
        }
        String username = authContext.getPrincipalName().orElse(null);
        if (username != null && getUserByUserName.execute(username).termsAccepted()) {
            markAccepted(session);
            return;
        }
        event.rerouteTo(TermsView.class);
    }
}

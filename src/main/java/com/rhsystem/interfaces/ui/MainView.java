package com.rhsystem.interfaces.ui;

import com.rhsystem.interfaces.ui.usuario.UserPage;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

/**
 * Rota raiz: redireciona para a lista de usuarios.
 */
@Route("")
@PermitAll
public class MainView extends Div implements BeforeEnterObserver {

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.forwardTo(UserPage.class);
    }
}

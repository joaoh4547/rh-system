package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.interfaces.ui.shared.ActionsGrid;
import com.rhsystem.interfaces.ui.shared.ObjectAction;
import com.rhsystem.interfaces.ui.shared.ObjectActions;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.Collection;
import java.util.function.Function;

/**
 * A specialized grid implementation for managing and displaying {@link User} entities.
 * Extends {@link ActionsGrid} to incorporate user-specific columns and actions.
 * <p>
 * This class provides columns for user attributes such as username, full name, email,
 * CPF (formatted), and status (represented as a styled badge). Additionally, it supports
 * operations via action buttons (e.g., edit and remove) defined by the supplied
 * {@link ObjectActions} instance.
 */
public class UserGrid extends ActionsGrid<User> {


    protected UserGrid(Collection<ObjectAction<User>> objectActions) {
        super(objectActions);
    }

    @Override
    protected void configColumns() {
        addColumn("username")
                .setHeader(getTranslation("col.username")).setAutoWidth(true);

        addColumn("firstName", User::getFullName)
                .setHeader(getTranslation("col.name")).setAutoWidth(true).setSortable(true);

        addColumn("email", User::getEmail)
                .setHeader(getTranslation("col.email")).setAutoWidth(true);

        addColumn("cpf", u -> formatCpf(u.getCpf()))
                .setHeader(getTranslation("col.cpf")).setAutoWidth(true);

        addColumn(new ComponentRenderer<>(this::statusBadge))
                .setHeader(getTranslation("col.status")).setAutoWidth(true);
    }

    @Override
    protected Function<User, String> createNameProvider() {
        return User::getFullName;
    }

    private Span statusBadge(User user) {
        var status = user.getStatus();
        var badge = new Span(getTranslation(status.getLabel()));
        String theme = switch (status) {
            case ACTIVE -> "badge success";
            case PENDING_CONFIRMATION -> "badge";
            case BLOCKED -> "badge error";
            case INACTIVE -> "badge contrast";
        };
        badge.getElement().setAttribute("theme", theme);
        return badge;
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}

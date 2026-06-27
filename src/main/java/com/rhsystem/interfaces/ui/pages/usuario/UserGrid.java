package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.ObjectActions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * User grid with pre-configured columns, formatting and row actions.
 *
 * <p>Receives an {@link ObjectActions} in the constructor — does not need to know
 * individual callbacks or the {@link UsuarioPage} that hosts it.
 */
public class UserGrid extends AppGrid<User> {

    public UserGrid(ObjectActions<User> actions) {
        addColumn("username")
                .setHeader(getTranslation("col.username")).setAutoWidth(true).setSortable(true);

        addColumn(User::getFullName)
                .setHeader(getTranslation("col.name")).setAutoWidth(true).setSortable(true);

        addColumn(User::getEmail)
                .setHeader(getTranslation("col.email")).setAutoWidth(true);

        addColumn(u -> formatCpf(u.getCpf()))
                .setHeader(getTranslation("col.cpf")).setAutoWidth(true);

        addColumn(new ComponentRenderer<>(this::statusBadge))
                .setHeader(getTranslation("col.status")).setAutoWidth(true);

        addColumn(new ComponentRenderer<>(u -> buildActions(u, actions)))
                .setHeader(getTranslation("col.actions")).setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
    }

    private Span statusBadge(User user) {
        var status = user.getStatus();
        var badge = new Span(getTranslation(status.getLabel()));
        String theme = switch (status) {
            case ACTIVE               -> "badge success";
            case PENDING_CONFIRMATION -> "badge";
            case BLOCKED              -> "badge error";
            case INACTIVE             -> "badge contrast";
        };
        badge.getElement().setAttribute("theme", theme);
        return badge;
    }

    private HorizontalLayout buildActions(User user, ObjectActions<User> actions) {
        var layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        if (actions.canEdit()) {
            var editButton = new Button(VaadinIcon.EDIT.create(),
                    e -> actions.edit(user));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON);
            editButton.getElement().setAttribute("title", getTranslation("col.tooltip.edit"));
            layout.add(editButton);
        }

        if (actions.canRemove()) {
            var removeButton = new Button(VaadinIcon.TRASH.create(),
                    e -> actions.remove(user, user.getFullName()));
            removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            removeButton.getElement().setAttribute("title", getTranslation("col.tooltip.remove"));
            layout.add(removeButton);
        }

        return layout;
    }

    private String formatCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}

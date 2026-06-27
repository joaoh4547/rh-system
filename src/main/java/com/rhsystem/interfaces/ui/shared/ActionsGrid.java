package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.domain.model.usuario.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

import java.util.function.Function;

public abstract class ActionsGrid<T> extends AppGrid<T> {


    protected void addActions(ObjectActions<T> actions) {
        addColumn(new ComponentRenderer<>(u -> buildActions(u, actions, createNameProvider())))
                .setHeader(getTranslation("col.actions")).setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
    }

    protected Function<T, String> defaultNameProvider = Object::toString;


    protected Function<T, String> createNameProvider() {
        return defaultNameProvider;
    }


    private HorizontalLayout buildActions(T obj, ObjectActions<T> actions, Function<T, String> nameProvider) {
        var layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        if (actions.canEdit()) {
            var editButton = new Button(VaadinIcon.EDIT.create(),
                    e -> actions.edit(obj));
            editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON);
            editButton.getElement().setAttribute("title", getTranslation("col.tooltip.edit"));
            layout.add(editButton);
        }

        if (actions.canRemove()) {
            var removeButton = new Button(VaadinIcon.TRASH.create(),
                    e -> actions.remove(obj, nameProvider.apply(obj)));
            removeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            removeButton.getElement().setAttribute("title", getTranslation("col.tooltip.remove"));
            layout.add(removeButton);
        }

        return layout;
    }


}

package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.domain.model.usuario.User;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.shared.Tooltip;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

/**
 * Abstract grid implementation that provides built-in support for action buttons (edit and remove)
 * within a grid's column. This class extends {@link AppGrid} to leverage its foundational styling and behavior.
 *
 * @param <T> the type of items displayed in the grid
 */
public abstract class ActionsGrid<T> extends AppGrid<T> {


    protected  ActionsGrid(Collection<ObjectAction<T>> actions) {
        configColumns();
        addActions(actions);
    }

    protected abstract void configColumns();

    /**
     * Adds a column to the grid for displaying action buttons (e.g., edit and remove)
     * associated with each row. The actions are defined by the provided {@code ObjectActions}.
     * The column is styled with specific header text, width, and alignment.
     *
     * @param actions an instance of {@link ObjectActions} detailing the set of actions
     *                (e.g., edit, remove) applicable to the items in the grid
     */

    protected final void addActions(Collection<ObjectAction<T>> actions) {
        addColumn(new ComponentRenderer<>(u -> buildActions(u, createNameProvider(), actions)))
                .setHeader(getTranslation("col.actions")).setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
    }

    /**
     * A default function used to determine the string representation of items in the grid.
     * This function is applied to grid items and provides their names or descriptions,
     * typically for display purposes. By default, this function uses the {@code toString}
     * method of the item.
     */
    protected Function<T, String> defaultNameProvider = Object::toString;


    protected Function<T, String> createNameProvider() {
        return defaultNameProvider;
    }


    /**
     * Constructs a horizontal layout that provides action buttons (edit and remove) for the specified object.
     * The buttons' availability is determined by the provided {@code ObjectActions}, and their behavior is
     * defined by the associated callbacks. The layout is visually styled and aligned appropriately.
     *
     * @param obj          the object for which action buttons are created
     * @param actions      an instance of {@link ObjectActions} that specifies the available actions for the object
     * @param nameProvider a function to generate a displayable name for the object, used in the remove action confirmation
     * @return a {@link HorizontalLayout} containing the action buttons
     */
    private HorizontalLayout buildActions(T obj, Function<T, String> nameProvider, Collection<ObjectAction<T>> actions) {
        var layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        actions.forEach(a -> {
            var button = new Button(a.getIcon().get(), e -> {
                a.getHandler().handle(obj);
            });
            button.setEnabled(a.getEnabled());
            button.setVisible(a.getVisible());
            button.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON);

            if(!CollectionUtils.isEmpty(a.getButtonVariants())){
                button.addThemeVariants(a.getButtonVariants().toArray(new ButtonVariant[0]));
            }

            Tooltip.forComponent(button).setText(a.getLabel());

            layout.add(button);
        });

        return layout;
    }


}

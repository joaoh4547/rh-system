package com.rhsystem.interfaces.ui.shared;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.ButtonVariant;
import lombok.*;

import java.util.Collection;
import java.util.function.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectAction<T> {

    /** Factory for the icon/component shown on the button — called once per grid row. */
    private Supplier<Component> icon;

    private String label;

    private ActionHandler<T> handler;

    @Singular
    private Collection<ButtonVariant> buttonVariants;

    @Builder.Default
    private Boolean enabled = true;

    @Builder.Default
    private Boolean visible = true;

    private Boolean primary;


    @FunctionalInterface
    public interface ActionHandler<T> {
        void handle(T target);

    }

}

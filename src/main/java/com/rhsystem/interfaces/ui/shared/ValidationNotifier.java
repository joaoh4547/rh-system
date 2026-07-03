package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.domain.validation.Violation;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * Renders a {@link ValidationException} as a single error notification
 * listing every violation (Notification Pattern on the UI side).
 *
 * <p>Usage inside a view: {@code ValidationNotifier.show(this::getTranslation, ex);}
 */
public final class ValidationNotifier {

    private static final int DURATION_MS = 6000;

    private ValidationNotifier() {
    }

    /**
     * Shows all violations of the exception in one error notification.
     *
     * @param translator translates message keys (pass {@code this::getTranslation})
     * @param exception  the validation exception carrying the violations
     */
    public static void show(UnaryOperator<String> translator, ValidationException exception) {
        List<String> messages = exception.getViolations().stream()
                .map(Violation::messageKey)
                .distinct()
                .map(translator)
                .toList();

        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setDuration(DURATION_MS);
        notification.setPosition(Notification.Position.MIDDLE);

        Div container = new Div();
        messages.forEach(message -> container.add(new Div(message)));
        notification.add(container);
        notification.open();
    }
}

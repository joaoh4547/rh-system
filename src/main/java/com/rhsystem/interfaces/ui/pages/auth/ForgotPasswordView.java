package com.rhsystem.interfaces.ui.pages.auth;

import com.rhsystem.application.usecase.usuario.RequestPasswordReset;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Password reset request: enter email and receive the reset link.
 */
@Route("forgot-password")
@AnonymousAllowed
public class ForgotPasswordView extends VerticalLayout implements HasDynamicTitle {

    private final EmailField email = new EmailField();

    public ForgotPasswordView(RequestPasswordReset requestPasswordReset) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        H3 title = new H3(getTranslation("forgot.title"));
        Paragraph text = new Paragraph(getTranslation("forgot.description"));
        text.getStyle().set("color", "var(--lumo-secondary-text-color)").set("text-align", "center");

        email.setLabel(getTranslation("forgot.field.email"));
        email.setWidthFull();
        email.setClearButtonVisible(true);

        Button sendButton = new Button(getTranslation("forgot.button"), e -> {
            requestPasswordReset.execute(email.getValue());
            Notification.show(getTranslation("forgot.success"),
                    5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        sendButton.setWidthFull();

        Anchor back = new Anchor("login", getTranslation("forgot.back"));
        back.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Div card = new Div(brand, title, text, email, sendButton, back);
        card.addClassName("login-box");
        add(card);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("forgot.page.title");
    }
}

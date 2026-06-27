package com.rhsystem.interfaces.ui.pages.auth;

import com.rhsystem.application.dto.usuario.ActivationCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.usecase.usuario.ResetPassword;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Sets a new password from the reset link: /reset-password/{token}.
 */
@Route("reset-password")
@AnonymousAllowed
public class ResetPasswordView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private final ResetPassword resetPassword;
    private final PasswordField password     = new PasswordField();
    private final PasswordField confirmation = new PasswordField();
    private String token;

    public ResetPasswordView(ResetPassword resetPassword) {
        this.resetPassword = resetPassword;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        H3 title = new H3(getTranslation("reset.title"));
        password.setLabel(getTranslation("reset.field.password"));
        password.setHelperText(getTranslation("reset.field.password.hint"));
        password.setWidthFull();
        confirmation.setLabel(getTranslation("reset.field.confirm"));
        confirmation.setWidthFull();

        Button saveButton = new Button(getTranslation("reset.button"), e -> reset());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.setWidthFull();

        Div card = new Div(brand, title, password, confirmation, saveButton);
        card.addClassName("login-box");
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, String token) {
        this.token = token;
    }

    private void reset() {
        try {
            resetPassword.execute(new ActivationCommand(token, password.getValue(), confirmation.getValue()));
            Notification.show(getTranslation("reset.success"), 5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        } catch (BusinessException ex) {
            Notification.show(getTranslation(ex.getMessage()), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    @Override
    public String getPageTitle() {
        return getTranslation("reset.page.title");
    }
}

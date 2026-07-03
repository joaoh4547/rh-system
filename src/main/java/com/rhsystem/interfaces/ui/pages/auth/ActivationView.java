package com.rhsystem.interfaces.ui.pages.auth;

import com.rhsystem.application.dto.usuario.ActivationCommand;
import com.rhsystem.application.usecase.usuario.ActivateUser;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.interfaces.ui.shared.ValidationNotifier;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Account activation screen accessed via email link: /activate/{token}.
 * Asks for a password and confirmation to activate the account.
 */
@Route("activate")
@AnonymousAllowed
public class ActivationView extends VerticalLayout implements HasUrlParameter<String>, HasDynamicTitle {

    private final ActivateUser activateUser;

    private final PasswordField password     = new PasswordField();
    private final PasswordField confirmation = new PasswordField();
    private final Button activateButton      = new Button();

    private String token;

    public ActivationView(ActivateUser activateUser) {
        this.activateUser = activateUser;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        password.setLabel(getTranslation("activation.field.password"));
        password.setHelperText(getTranslation("activation.field.password.hint"));
        password.setWidth("320px");
        confirmation.setLabel(getTranslation("activation.field.confirm"));
        confirmation.setWidth("320px");

        activateButton.setText(getTranslation("activation.button"));
        activateButton.addClickListener(e -> activate());
        add(new H2(getTranslation("activation.title")),
            new Paragraph(getTranslation("activation.description")),
            password, confirmation, activateButton);
    }

    @Override
    public String getPageTitle() {
        return getTranslation("activation.page.title");
    }

    @Override
    public void setParameter(BeforeEvent event, String token) {
        this.token = token;
    }

    private void activate() {
        try {
            activateUser.execute(new ActivationCommand(token, password.getValue(), confirmation.getValue()));
            Notification.show(getTranslation("activation.success"), 5000,
                    Notification.Position.MIDDLE);
            activateButton.getUI().ifPresent(ui -> ui.navigate("login"));
        } catch (ValidationException ex) {
            ValidationNotifier.show(this::getTranslation, ex);
        }
    }
}

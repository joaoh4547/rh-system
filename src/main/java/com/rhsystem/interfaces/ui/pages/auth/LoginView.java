package com.rhsystem.interfaces.ui.pages.auth;

import com.rhsystem.application.dto.login.LoginResult;
import com.rhsystem.application.usecase.usuario.AcceptTerms;
import com.rhsystem.application.usecase.usuario.ValidateLogin;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login screen. The user is only authenticated (native POST to Spring Security) after
 * credentials are validated and, if needed, terms are accepted.
 */
@Route("login")
@PageTitle("Login - RH System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final ValidateLogin validateLogin;
    private final AcceptTerms acceptTerms;
    private final LoginForm loginForm = new LoginForm();

    public LoginView(ValidateLogin validateLogin, AcceptTerms acceptTerms) {
        this.validateLogin = validateLogin;
        this.acceptTerms   = acceptTerms;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        Span subtitle = new Span(getTranslation("login.subtitle"));
        subtitle.addClassName("login-subtitle");

        loginForm.setForgotPasswordButtonVisible(true);
        loginForm.addForgotPasswordListener(e ->
                getUI().ifPresent(ui -> ui.navigate("forgot-password")));
        loginForm.addLoginListener(e -> processLogin(e.getUsername(), e.getPassword()));
        loginForm.setI18n(buildI18n());

        Div card = new Div(brand, subtitle, loginForm);
        card.addClassName("login-box");
        add(card);

        removeAutofillYellow();
    }

    private void processLogin(String username, String password) {
        LoginResult result = validateLogin.execute(username, password);
        switch (result) {
            case INVALID_CREDENTIALS -> {
                loginForm.setError(true);
                loginForm.setEnabled(true);
            }
            case TERMS_PENDING -> openTermsDialog(username, password);
            case OK            -> authenticate(username, password);
        }
    }

    private void openTermsDialog(String username, String password) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(getTranslation("login.terms.title"));
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setWidth("560px");

        Div content = new Div();
        content.getStyle().set("max-height", "320px").set("overflow", "auto")
                .set("line-height", "1.55").set("color", "var(--lumo-secondary-text-color)");
        content.add(new Paragraph("To access RH System you must read and accept the Terms of Use "
                + "and Privacy Policy."));
        content.add(new Paragraph("By accepting, you agree to the processing of your personal data "
                + "strictly for human-resources management purposes, in compliance with applicable "
                + "data-protection law. Your data will not be shared with third parties without a "
                + "legal basis."));
        content.add(new Paragraph("You may request review or deletion of your data from system "
                + "administrators at any time."));
        dialog.add(content);

        Button decline = new Button(getTranslation("login.terms.decline"), e -> {
            dialog.close();
            loginForm.setEnabled(true);
            Notification.show(getTranslation("login.terms.declined"))
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        });
        decline.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Button accept = new Button(getTranslation("login.terms.accept"), e -> {
            acceptTerms.execute(username);
            dialog.close();
            authenticate(username, password);
        });
        accept.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(decline, accept);
        dialog.open();
    }

    /** Completes login by submitting a native POST to Spring Security. */
    private void authenticate(String username, String password) {
        getElement().executeJs(
            "const f=document.createElement('form');" +
            "f.method='post'; f.action='login'; f.style.display='none';" +
            "const u=document.createElement('input'); u.name='username'; u.value=$0; f.appendChild(u);" +
            "const p=document.createElement('input'); p.name='password'; p.value=$1; f.appendChild(p);" +
            "document.body.appendChild(f); f.submit();",
            username, password);
    }

    private LoginI18n buildI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form form = i18n.getForm();
        form.setTitle(getTranslation("login.form.title"));
        form.setUsername(getTranslation("login.form.username"));
        form.setPassword(getTranslation("login.form.password"));
        form.setSubmit(getTranslation("login.form.submit"));
        form.setForgotPassword(getTranslation("login.form.forgot"));
        i18n.setForm(form);

        LoginI18n.ErrorMessage error = new LoginI18n.ErrorMessage();
        error.setTitle(getTranslation("login.error.title"));
        error.setMessage(getTranslation("login.error.message"));
        i18n.setErrorMessage(error);
        return i18n;
    }

    private void removeAutofillYellow() {
        getElement().executeJs(
            "const css = `[part=\"input-field\"]{background-color: var(--field-bg) !important;}" +
            "::slotted(input:-webkit-autofill),input:-webkit-autofill{" +
            "-webkit-text-fill-color: var(--lumo-body-text-color) !important;" +
            "-webkit-box-shadow: inset 0 0 0 1000px var(--field-bg) !important;" +
            "box-shadow: inset 0 0 0 1000px var(--field-bg) !important;" +
            "caret-color: var(--lumo-body-text-color) !important;" +
            "transition: background-color 600000s 0s, color 600000s 0s !important;}`;" +
            "function inject(root){" +
            "  if(!root || !root.querySelectorAll) return;" +
            "  root.querySelectorAll('vaadin-text-field,vaadin-password-field,vaadin-email-field').forEach(f=>{" +
            "    if(f.shadowRoot && !f.shadowRoot.querySelector('style[data-noautofill]')){" +
            "      const s=document.createElement('style'); s.setAttribute('data-noautofill','');" +
            "      s.textContent=css; f.shadowRoot.appendChild(s);" +
            "    }});" +
            "  root.querySelectorAll('*').forEach(el=>{ if(el.shadowRoot) inject(el.shadowRoot); });" +
            "}" +
            "inject(document);" +
            "setTimeout(()=>inject(document),300);" +
            "setTimeout(()=>inject(document),1200);"
        );
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            loginForm.setError(true);
        }
    }
}

package com.rhsystem.interfaces.ui.pages.auth;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login screen.
 *
 * <p>The {@link LoginForm} posts <b>natively</b> to Spring Security ({@code POST /login}):
 * the password goes straight from the browser to the authentication filter and never
 * travels through the Vaadin server-side round-trip (previously it was validated on
 * the server and re-injected into the DOM to build a hidden form).</p>
 *
 * <p>Terms of use are handled <b>after</b> authentication by
 * {@link com.rhsystem.interfaces.ui.security.TermsAcceptanceGuard}, which reroutes
 * to {@link TermsView} until the user accepts them. Failed logins come back as
 * {@code /login?error} with a generic message (no hint whether the user exists).</p>
 */
@Route("login")
@PageTitle("Login - RH System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        Span subtitle = new Span(getTranslation("login.subtitle"));
        subtitle.addClassName("login-subtitle");

        loginForm.setAction("login"); // native POST to Spring Security
        loginForm.setForgotPasswordButtonVisible(true);
        loginForm.addForgotPasswordListener(e ->
                getUI().ifPresent(ui -> ui.navigate("forgot-password")));
        loginForm.setI18n(buildI18n());

        Div card = new Div(brand, subtitle, loginForm);
        card.addClassName("login-box");
        add(card);

        removeAutofillYellow();
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

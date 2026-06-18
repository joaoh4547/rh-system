package com.rhsystem.interfaces.ui;

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
 * Tela de login no estilo AdminLTE (login box centralizado).
 */
@Route("login")
@PageTitle("Login - RH System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        Span subtitle = new Span("Faça login para iniciar a sessão");
        subtitle.addClassName("login-subtitle");

        login.setAction("login");
        login.setI18n(criarI18n());

        Div card = new Div(brand, subtitle, login);
        card.addClassName("login-box");
        add(card);

        removerAutofillAmarelo();
    }

    /**
     * Injeta um &lt;style&gt; dentro do shadow DOM dos campos (vaadin-text-field /
     * password / email) para mascarar o fundo amarelo do autofill do navegador.
     * Funciona independentemente do bundle do tema.
     */
    private void removerAutofillAmarelo() {
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

    private LoginI18n criarI18n() {
        LoginI18n i18n = LoginI18n.createDefault();
        LoginI18n.Form form = i18n.getForm();
        form.setTitle("Entrar");
        form.setUsername("Usuário");
        form.setPassword("Senha");
        form.setSubmit("Entrar");
        form.setForgotPassword("Esqueci minha senha");
        i18n.setForm(form);

        LoginI18n.ErrorMessage erro = new LoginI18n.ErrorMessage();
        erro.setTitle("Falha no login");
        erro.setMessage("Usuário ou senha inválidos, ou conta não ativada.");
        i18n.setErrorMessage(erro);
        return i18n;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}

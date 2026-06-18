package com.rhsystem.interfaces.ui;

import com.rhsystem.application.dto.ResultadoLogin;
import com.rhsystem.application.usecase.usuario.AceitarTermos;
import com.rhsystem.application.usecase.usuario.ValidarLogin;
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
 * Tela de login. O usuário só é autenticado de fato (POST nativo para o Spring
 * Security) após validar as credenciais e, se necessário, aceitar os termos —
 * mantendo-o na tela de login enquanto não aceitar.
 */
@Route("login")
@PageTitle("Login - RH System")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final ValidarLogin validarLogin;
    private final AceitarTermos aceitarTermos;
    private final LoginForm login = new LoginForm();

    public LoginView(ValidarLogin validarLogin, AceitarTermos aceitarTermos) {
        this.validarLogin = validarLogin;
        this.aceitarTermos = aceitarTermos;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        Span subtitle = new Span("Faça login para iniciar a sessão");
        subtitle.addClassName("login-subtitle");

        // Sem setAction: o LoginForm dispara LoginEvent no servidor (não faz POST sozinho).
        login.setForgotPasswordButtonVisible(true);
        login.addForgotPasswordListener(e ->
                getUI().ifPresent(ui -> ui.navigate("esqueci-senha")));
        login.addLoginListener(e -> processarLogin(e.getUsername(), e.getPassword()));
        login.setI18n(criarI18n());

        Div card = new Div(brand, subtitle, login);
        card.addClassName("login-box");
        add(card);

        removerAutofillAmarelo();
    }

    private void processarLogin(String username, String senha) {
        ResultadoLogin resultado = validarLogin.executar(username, senha);
        switch (resultado) {
            case CREDENCIAIS_INVALIDAS -> {
                login.setError(true);
                login.setEnabled(true);
            }
            case TERMOS_PENDENTES -> abrirDialogTermos(username, senha);
            case OK -> autenticar(username, senha);
        }
    }

    private void abrirDialogTermos(String username, String senha) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Termos de Uso");
        dialog.setModal(true);
        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);
        dialog.setWidth("560px");

        Div texto = new Div();
        texto.getStyle().set("max-height", "320px").set("overflow", "auto")
                .set("line-height", "1.55").set("color", "var(--lumo-secondary-text-color)");
        texto.add(new Paragraph("Para acessar o RH System, você precisa ler e aceitar os Termos de Uso "
                + "e a Política de Privacidade."));
        texto.add(new Paragraph("Ao aceitar, você concorda com o tratamento dos seus dados pessoais "
                + "estritamente para as finalidades de gestão de recursos humanos, em conformidade com a "
                + "LGPD. Seus dados não serão compartilhados com terceiros sem base legal."));
        texto.add(new Paragraph("Você pode solicitar a qualquer momento a revisão ou exclusão dos seus "
                + "dados aos administradores do sistema."));
        dialog.add(texto);

        Button recusar = new Button("Recusar", e -> {
            dialog.close();
            login.setEnabled(true);
            Notification.show("É necessário aceitar os termos para acessar o sistema.")
                    .addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        });
        recusar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);

        Button aceitar = new Button("Li e aceito", e -> {
            aceitarTermos.executar(username);
            dialog.close();
            autenticar(username, senha);
        });
        aceitar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        dialog.getFooter().add(recusar, aceitar);
        dialog.open();
    }

    /** Efetiva o login enviando um POST nativo para o Spring Security. */
    private void autenticar(String username, String senha) {
        getElement().executeJs(
            "const f=document.createElement('form');" +
            "f.method='post'; f.action='login'; f.style.display='none';" +
            "const u=document.createElement('input'); u.name='username'; u.value=$0; f.appendChild(u);" +
            "const p=document.createElement('input'); p.name='password'; p.value=$1; f.appendChild(p);" +
            "document.body.appendChild(f); f.submit();",
            username, senha);
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

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}

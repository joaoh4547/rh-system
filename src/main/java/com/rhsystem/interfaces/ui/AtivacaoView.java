package com.rhsystem.interfaces.ui;

import com.rhsystem.application.dto.AtivacaoCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.service.UsuarioService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Tela de ativação acessada pelo link do email: /ativar/{token}.
 * Solicita a senha e a confirmação para ativar a conta.
 */
@Route("ativar")
@PageTitle("Ativação de conta - RH System")
@AnonymousAllowed
public class AtivacaoView extends VerticalLayout implements HasUrlParameter<String> {

    private final UsuarioService usuarioService;

    private final PasswordField senha = new PasswordField("Nova senha");
    private final PasswordField confirmacao = new PasswordField("Confirme a senha");
    private final Button ativar = new Button("Ativar conta");

    private String token;

    public AtivacaoView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        senha.setHelperText("Mínimo de 6 caracteres");
        senha.setWidth("320px");
        confirmacao.setWidth("320px");

        ativar.addClickListener(e -> ativar());
        add(new H2("Ativação de conta"),
            new Paragraph("Defina sua senha para concluir o cadastro."),
            senha, confirmacao, ativar);
    }

    @Override
    public void setParameter(BeforeEvent event, String token) {
        this.token = token;
    }

    private void ativar() {
        try {
            usuarioService.ativar(new AtivacaoCommand(token, senha.getValue(), confirmacao.getValue()));
            Notification.show("Conta ativada com sucesso! Você já pode fazer login.", 5000,
                    Notification.Position.MIDDLE);
            ativar.getUI().ifPresent(ui -> ui.navigate("login"));
        } catch (RegraNegocioException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }
}

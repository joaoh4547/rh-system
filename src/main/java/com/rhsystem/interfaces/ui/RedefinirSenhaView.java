package com.rhsystem.interfaces.ui;

import com.rhsystem.application.dto.AtivacaoCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.usecase.usuario.RedefinirSenha;
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
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Define uma nova senha a partir do link de redefinição: /redefinir/{token}.
 */
@Route("redefinir")
@PageTitle("Redefinir senha - RH System")
@AnonymousAllowed
public class RedefinirSenhaView extends VerticalLayout implements HasUrlParameter<String> {

    private final RedefinirSenha redefinirSenha;
    private final PasswordField senha = new PasswordField("Nova senha");
    private final PasswordField confirmacao = new PasswordField("Confirme a senha");
    private String token;

    public RedefinirSenhaView(RedefinirSenha redefinirSenha) {
        this.redefinirSenha = redefinirSenha;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        H3 titulo = new H3("Redefinir senha");
        senha.setHelperText("Mínimo de 6 caracteres");
        senha.setWidthFull();
        confirmacao.setWidthFull();

        Button salvar = new Button("Salvar nova senha", e -> redefinir());
        salvar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        salvar.setWidthFull();

        Div card = new Div(brand, titulo, senha, confirmacao, salvar);
        card.addClassName("login-box");
        add(card);
    }

    @Override
    public void setParameter(BeforeEvent event, String token) {
        this.token = token;
    }

    private void redefinir() {
        try {
            redefinirSenha.executar(new AtivacaoCommand(token, senha.getValue(), confirmacao.getValue()));
            Notification.show("Senha redefinida com sucesso! Você já pode entrar.", 5000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        } catch (RegraNegocioException ex) {
            Notification.show(ex.getMessage(), 5000, Notification.Position.MIDDLE)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}

package com.rhsystem.interfaces.ui;

import com.rhsystem.application.usecase.usuario.SolicitarRedefinicaoSenha;
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
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Solicitação de redefinição de senha: informa o email e recebe o link.
 */
@Route("esqueci-senha")
@PageTitle("Recuperar senha - RH System")
@AnonymousAllowed
public class EsqueciSenhaView extends VerticalLayout {

    private final EmailField email = new EmailField("Email");

    public EsqueciSenhaView(SolicitarRedefinicaoSenha solicitarRedefinicaoSenha) {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        addClassName("login-bg");

        Span brand = new Span(VaadinIcon.CUBES.create(), new Span(" RH System"));
        brand.addClassName("login-brand");

        H3 titulo = new H3("Recuperar senha");
        Paragraph texto = new Paragraph(
                "Informe o email da sua conta. Se ele estiver cadastrado, enviaremos um link para criar uma nova senha.");
        texto.getStyle().set("color", "var(--lumo-secondary-text-color)").set("text-align", "center");

        email.setWidthFull();
        email.setClearButtonVisible(true);

        Button enviar = new Button("Enviar link", e -> {
            solicitarRedefinicaoSenha.executar(email.getValue());
            Notification.show("Se o email estiver cadastrado, enviamos as instruções de redefinição.",
                    5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            getUI().ifPresent(ui -> ui.navigate("login"));
        });
        enviar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        enviar.setWidthFull();

        Anchor voltar = new Anchor("login", "Voltar ao login");
        voltar.getStyle().set("font-size", "var(--lumo-font-size-s)");

        Div card = new Div(brand, titulo, texto, email, enviar, voltar);
        card.addClassName("login-box");
        add(card);
    }
}

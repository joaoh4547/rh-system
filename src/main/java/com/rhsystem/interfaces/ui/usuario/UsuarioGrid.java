package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.ObjectActions;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

/**
 * Grid de usuários com colunas, formatações e ações pré-configuradas.
 *
 * <p>Recebe um {@link ObjectActions} no construtor — não precisa conhecer
 * callbacks individuais nem a {@link UsuarioPage} que a hospeda.
 *
 * <pre>{@code
 * // A Page não precisa mais passar callbacks avulsos:
 * return new UsuarioGrid(actions);
 * }</pre>
 */
public class UsuarioGrid extends AppGrid<Usuario> {

    public UsuarioGrid(ObjectActions<Usuario> actions) {
        addColumn(Usuario::getUsername)
                .setHeader("Username").setAutoWidth(true).setSortable(true);

        addColumn(Usuario::getNomeCompleto)
                .setHeader("Nome").setAutoWidth(true).setSortable(true);

        addColumn(Usuario::getEmail)
                .setHeader("Email").setAutoWidth(true);

        addColumn(u -> formatarCpf(u.getCpf()))
                .setHeader("CPF").setAutoWidth(true);

        addColumn(new ComponentRenderer<>(this::badgeStatus))
                .setHeader("Status").setAutoWidth(true);

        addColumn(new ComponentRenderer<>(u -> criarAcoes(u, actions)))
                .setHeader("Ações").setAutoWidth(true).setFlexGrow(0)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
    }

    // ── Renderizadores ────────────────────────────────────────────────────────

    private Span badgeStatus(Usuario usuario) {
        var status = usuario.getStatus();
        var badge  = new Span(status.getDescricao());
        String theme = switch (status) {
            case ATIVO                -> "badge success";
            case PENDENTE_CONFIRMACAO -> "badge";
            case BLOQUEADO            -> "badge error";
            case INATIVO              -> "badge contrast";
        };
        badge.getElement().setAttribute("theme", theme);
        return badge;
    }

    private HorizontalLayout criarAcoes(Usuario usuario, ObjectActions<Usuario> actions) {
        var layout = new HorizontalLayout();
        layout.setSpacing(false);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        if (actions.podeEditar()) {
            var editar = new Button(VaadinIcon.EDIT.create(),
                    e -> actions.editar(usuario));
            editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON);
            editar.getElement().setAttribute("title", "Editar");
            layout.add(editar);
        }

        if (actions.podeRemover()) {
            var remover = new Button(VaadinIcon.TRASH.create(),
                    e -> actions.remover(usuario, usuario.getNomeCompleto()));
            remover.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL,
                    ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
            remover.getElement().setAttribute("title", "Remover");
            layout.add(remover);
        }

        return layout;
    }

    // ── Formatação ────────────────────────────────────────────────────────────

    private String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) return cpf;
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}

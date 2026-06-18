package com.rhsystem.interfaces.ui;

import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.service.UsuarioService;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.interfaces.ui.component.StatCard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;

import jakarta.annotation.security.PermitAll;

/**
 * Lista de usuários com ações de CRUD por linha.
 */
@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuários - RH System")
@PermitAll
public class UsuarioListView extends VerticalLayout {

    private final UsuarioService usuarioService;
    private final Grid<Usuario> grid = new Grid<>(Usuario.class, false);

    public UsuarioListView(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
        setSizeFull();
        setPadding(true);
        setSpacing(true);
        addClassName("view");

        configurarGrid();

        add(criarCabecalho(), statsContainer, card);
        card.add(criarToolbar(), grid);
        card.addClassName("card");
        card.setSizeFull();
        setFlexGrow(1, card);

        atualizar();
    }

    private final com.vaadin.flow.component.html.Div statsContainer =
            new com.vaadin.flow.component.html.Div();
    private final com.vaadin.flow.component.html.Div card =
            new com.vaadin.flow.component.html.Div();

    private com.vaadin.flow.component.html.Div criarCabecalho() {
        com.vaadin.flow.component.html.H2 titulo = new com.vaadin.flow.component.html.H2("Usuários");
        titulo.addClassNames(LumoUtility.Margin.NONE);
        com.vaadin.flow.component.html.Span sub =
                new com.vaadin.flow.component.html.Span("Gerenciamento de usuários do sistema");
        sub.addClassName("page-subtitle");
        com.vaadin.flow.component.html.Div h =
                new com.vaadin.flow.component.html.Div(titulo, sub);
        h.addClassName("page-header");
        return h;
    }

    private void atualizarStats(java.util.List<Usuario> usuarios) {
        long total = usuarios.size();
        long ativos = usuarios.stream().filter(u -> u.getStatus() == StatusUsuario.ATIVO).count();
        long pendentes = usuarios.stream().filter(u -> u.getStatus() == StatusUsuario.PENDENTE_CONFIRMACAO).count();
        long bloqueados = usuarios.stream().filter(u -> u.getStatus() == StatusUsuario.BLOQUEADO).count();

        statsContainer.removeAll();
        statsContainer.addClassName("stats-grid");
        statsContainer.add(
                new StatCard("Total de usuários", total, VaadinIcon.USERS, StatCard.Accent.PRIMARY),
                new StatCard("Ativos", ativos, VaadinIcon.CHECK_CIRCLE, StatCard.Accent.SUCCESS),
                new StatCard("Pendentes", pendentes, VaadinIcon.HOURGLASS, StatCard.Accent.WARNING),
                new StatCard("Bloqueados", bloqueados, VaadinIcon.BAN, StatCard.Accent.DANGER));
    }

    private HorizontalLayout criarToolbar() {
        Span titulo = new Span("Lista de usuários");
        titulo.addClassName("card-title");

        Button novo = new Button("Novo usuário", VaadinIcon.PLUS.create(), e -> abrirFormulario(null));
        novo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(titulo, novo);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.expand(titulo);
        toolbar.addClassName("card-toolbar");
        return toolbar;
    }

    private void configurarGrid() {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addColumn(Usuario::getUsername).setHeader("Username").setAutoWidth(true).setSortable(true);
        grid.addColumn(Usuario::getNomeCompleto).setHeader("Nome").setAutoWidth(true).setSortable(true);
        grid.addColumn(Usuario::getEmail).setHeader("Email").setAutoWidth(true);
        grid.addColumn(u -> formatarCpf(u.getCpf())).setHeader("CPF").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::criarBadgeStatus)).setHeader("Status").setAutoWidth(true);
        grid.addColumn(new ComponentRenderer<>(this::criarAcoes))
                .setHeader("Ações").setAutoWidth(true).setFlexGrow(0).setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);
        grid.setSizeFull();
    }

    private HorizontalLayout criarAcoes(Usuario usuario) {
        Button editar = new Button(VaadinIcon.EDIT.create(), e -> abrirFormulario(usuario));
        editar.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        editar.getElement().setAttribute("title", "Editar");

        Button excluir = new Button(VaadinIcon.TRASH.create(), e -> confirmarExclusao(usuario));
        excluir.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_ERROR);
        excluir.getElement().setAttribute("title", "Excluir");

        HorizontalLayout acoes = new HorizontalLayout(editar, excluir);
        acoes.setSpacing(false);
        acoes.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        return acoes;
    }

    private Span criarBadgeStatus(Usuario usuario) {
        Span badge = new Span(usuario.getStatus().getDescricao());
        String tema = switch (usuario.getStatus()) {
            case ATIVO -> "badge success";
            case INATIVO -> "badge contrast";
            case BLOQUEADO -> "badge error";
            case PENDENTE_CONFIRMACAO -> "badge";
        };
        badge.getElement().getThemeList().add(tema);
        return badge;
    }

    private void atualizar() {
        java.util.List<Usuario> usuarios = usuarioService.listar();
        grid.setItems(usuarios);
        atualizarStats(usuarios);
    }

    private void abrirFormulario(Usuario usuario) {
        new UsuarioFormDialog(usuarioService, usuario, this::atualizar).open();
    }

    private void confirmarExclusao(Usuario usuario) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Excluir usuário");
        dialog.setText("Deseja realmente excluir \"" + usuario.getNomeCompleto() + "\"? Esta ação não pode ser desfeita.");
        dialog.setCancelable(true);
        dialog.setCancelText("Cancelar");
        dialog.setConfirmText("Excluir");
        dialog.setConfirmButtonTheme("error primary");
        dialog.addConfirmListener(e -> excluir(usuario));
        dialog.open();
    }

    private void excluir(Usuario usuario) {
        try {
            usuarioService.remover(usuario.getId());
            Notification.show("Usuário excluído.").addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            atualizar();
        } catch (RegraNegocioException ex) {
            Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private static String formatarCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "."
                + cpf.substring(6, 9) + "-" + cpf.substring(9);
    }
}

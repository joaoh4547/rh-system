package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.application.usecase.usuario.AtualizarUsuario;
import com.rhsystem.application.usecase.usuario.BuscarResumoUsuarios;
import com.rhsystem.application.usecase.usuario.CriarUsuario;
import com.rhsystem.application.usecase.usuario.ListarUsuarios;
import com.rhsystem.application.usecase.usuario.RemoverUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.StatCard;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.BasePage;
import com.rhsystem.interfaces.ui.shared.ObjectActions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;

/**
 * Página de gerenciamento de usuários do sistema.
 *
 * <p>O grid carrega usuários com paginação a nível de banco ({@link ListarUsuarios}).
 * Os KPIs são calculados por {@link BuscarResumoUsuarios} via contagens agregadas —
 * independentes da página corrente do grid.
 */
@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Usuários - RH System")
@PermitAll
public class UsuarioPage extends BasePage<Usuario> {

    private final ListarUsuarios listarUsuarios;
    private final CriarUsuario criarUsuario;
    private final AtualizarUsuario atualizarUsuario;
    private final RemoverUsuario removerUsuario;
    private final BuscarResumoUsuarios buscarResumo;

    public UsuarioPage(ListarUsuarios listarUsuarios,
                       CriarUsuario criarUsuario,
                       AtualizarUsuario atualizarUsuario,
                       RemoverUsuario removerUsuario,
                       BuscarResumoUsuarios buscarResumo) {
        this.listarUsuarios   = listarUsuarios;
        this.criarUsuario     = criarUsuario;
        this.atualizarUsuario = atualizarUsuario;
        this.removerUsuario   = removerUsuario;
        this.buscarResumo     = buscarResumo;
    }

    // ── Metadados ─────────────────────────────────────────────────────────────

    @Override protected String tituloPagina()    { return "Usuários"; }
    @Override protected String subtituloPagina() { return "Gerenciamento de usuários do sistema"; }
    @Override protected String tituloTabela()    { return "Lista de usuários"; }
    @Override protected String labelBotaoNovo()  { return "Novo usuário"; }

    // ── DataProvider (paginação server-side) ──────────────────────────────────

    @Override
    protected DataProvider<Usuario, Void> criarDataProvider() {
        return DataProvider.fromCallbacks(
                query -> listarUsuarios.executar(query.getOffset(), query.getLimit()),
                query -> Math.toIntExact(buscarResumo.executar().total())
        );
    }

    // ── KPIs (use case de agregação dedicado) ─────────────────────────────────

    @Override
    protected Component criarStats() {
        var resumo = buscarResumo.executar();
        var container = new Div(
                new StatCard("Total de usuários", resumo.total(),     VaadinIcon.USERS,        StatCard.Accent.PRIMARY),
                new StatCard("Ativos",            resumo.ativos(),    VaadinIcon.CHECK_CIRCLE,  StatCard.Accent.SUCCESS),
                new StatCard("Pendentes",         resumo.pendentes(), VaadinIcon.HOURGLASS,     StatCard.Accent.WARNING),
                new StatCard("Bloqueados",        resumo.bloqueados(),VaadinIcon.BAN,           StatCard.Accent.DANGER)
        );
        container.addClassName("stats-grid");
        return container;
    }

    // ── Grid ──────────────────────────────────────────────────────────────────

    @Override
    protected AppGrid<Usuario> criarDataGrid(ObjectActions<Usuario> actions) {
        return new UsuarioGrid(actions);
    }

    // ── Formulário ────────────────────────────────────────────────────────────

    @Override
    protected Dialog criarFormulario(@Nullable Usuario usuario) {
        return new UsuarioFormDialog(criarUsuario, atualizarUsuario, usuario, this::atualizar);
    }

    // ── Persistência ──────────────────────────────────────────────────────────

    @Override
    protected void remover(Usuario usuario) {
        removerUsuario.executar(usuario.getId());
    }
}

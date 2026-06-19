package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.application.exception.RegraNegocioException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;

/**
 * Página base para CRUDs com persistência via serviços/use-cases.
 *
 * <p><b>Responsabilidades desta camada:</b>
 * <ul>
 *   <li>Layout de página completo (cabeçalho, KPIs, card com grid).</li>
 *   <li>Grid paginado <b>a nível de banco</b> via {@link DataProvider#fromCallbacks}.</li>
 *   <li>KPIs calculados por use case dedicado — não dependem da lista carregada.</li>
 *   <li>Ciclo de persistência: {@link #remover(Object)}.</li>
 * </ul>
 *
 * <p><b>Diferença em relação ao {@link DataEditor}:</b>
 * {@code DataEditor} mantém dados em memória (para sub-editores embutidos em formulários).
 * {@code BasePage} <em>nunca</em> carrega todos os registros — delega sempre ao banco
 * via {@link DataProvider} e usa use cases agregados para os KPIs.
 *
 * <h3>Contrato da subclasse</h3>
 * <pre>{@code
 * @Route("usuarios")
 * public class UsuarioPage extends BasePage<Usuario> {
 *
 *     @Override
 *     protected DataProvider<Usuario, Void> criarDataProvider() {
 *         return DataProvider.fromCallbacks(
 *             q -> listarUsuarios.executar(q.getOffset(), q.getLimit()),
 *             q -> contarUsuarios.executar()
 *         );
 *     }
 *
 *     @Override
 *     protected Component criarStats() {
 *         var resumo = buscarResumo.executar();   // use case de agregação
 *         return new Div(new StatCard("Total", resumo.total(), ...));
 *     }
 *
 *     @Override
 *     protected void remover(Usuario u) { removerUsuario.executar(u.getId()); }
 * }
 * }</pre>
 *
 * @param <T> tipo do registro gerenciado pela página
 */
public abstract class BasePage<T> extends DataEditor<T> {

    private final Div statsContainer = new Div();
    private final Div card           = new Div();

    private DataProvider<T, Void> dataProvider;

    protected BasePage() {
        addClassName("view");
        setPadding(true);
        setSpacing(true);
    }

    /** Inicializado pelo Spring após DI da subclasse — garante use cases disponíveis. */
    @PostConstruct
    private void inicializar() {
        grid = criarDataGrid(criarObjectActions());
        dataProvider = criarDataProvider();
        grid.setDataProvider(dataProvider);
        construirLayout();
        atualizarKpis();
    }

    /** Desabilita o onAttach do {@link DataEditor}: BasePage usa @PostConstruct. */
    @Override
    protected void onAttach(AttachEvent event) {
        // intencional: inicialização via @PostConstruct
    }

    @Override
    protected void construirLayout() {
        statsContainer.addClassName("stats-grid");
        card.addClassName("card");
        card.setSizeFull();
        setFlexGrow(1, card);
        card.add(criarToolbar(tituloTabela(), labelBotaoNovo()), grid);
        add(criarCabecalho(), statsContainer, card);
    }

    // ── Metadados ─────────────────────────────────────────────────────────────

    protected abstract String tituloPagina();
    protected abstract String subtituloPagina();

    @Override protected abstract String tituloTabela();
    @Override protected abstract String labelBotaoNovo();

    // ── DataProvider (paginação server-side) ──────────────────────────────────

    /**
     * Cria o {@link DataProvider} que alimenta o grid com paginação a nível de banco.
     * O Vaadin chama os callbacks com {@code offset/limit} a cada scroll ou ordenação.
     *
     * <pre>{@code
     * return DataProvider.fromCallbacks(
     *     q -> listarUsuarios.executar(q.getOffset(), q.getLimit()),
     *     q -> contarUsuarios.executar()
     * );
     * }</pre>
     */
    protected abstract DataProvider<T, Void> criarDataProvider();

    // ── KPIs ──────────────────────────────────────────────────────────────────

    /**
     * Retorna o componente de KPIs renderizado acima do card.
     *
     * <p>Implemente chamando um use case de agregação dedicado — não depende
     * da lista paginada do grid. Retorne {@code null} para omitir a área.
     *
     * <pre>{@code
     * @Override
     * protected Component criarStats() {
     *     var r = buscarResumoUsuarios.executar();
     *     return new Div(
     *         new StatCard("Total",     r.total(),      ...),
     *         new StatCard("Ativos",    r.ativos(),     ...),
     *         new StatCard("Pendentes", r.pendentes(),  ...)
     *     );
     * }
     * }</pre>
     */
    @Nullable
    protected Component criarStats() {
        return null;
    }

    // ── Persistência ──────────────────────────────────────────────────────────

    /**
     * Remove o registro via serviço/repositório.
     * Lança {@link RegraNegocioException} para sinalizar erros de negócio.
     */
    protected abstract void remover(T item);

    @Override
    protected void executarRemocao(T item) {
        try {
            remover(item);
            atualizar();
            notificarSucesso("Registro removido com sucesso.");
        } catch (RegraNegocioException e) {
            notificarErro(e.getMessage());
        }
    }

    /**
     * Recarrega o grid (dispara novo fetch/count no banco) e atualiza os KPIs.
     * Use {@code this::atualizar} como callback nos diálogos de formulário.
     */
    @Override
    public void atualizar() {
        dataProvider.refreshAll();
        atualizarKpis();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private Div criarCabecalho() {
        var titulo = new H2(tituloPagina());
        titulo.addClassNames(LumoUtility.Margin.NONE);
        var subtitulo = new Span(subtituloPagina());
        subtitulo.addClassName("page-subtitle");
        var cabecalho = new Div(titulo, subtitulo);
        cabecalho.addClassName("page-header");
        return cabecalho;
    }

    private void atualizarKpis() {
        statsContainer.removeAll();
        var stats = criarStats();
        if (stats != null) statsContainer.add(stats);
    }
}

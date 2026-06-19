package com.rhsystem.interfaces.ui.shared;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Componente de CRUD <b>em memória</b> — gerencia uma lista de itens do tipo {@code T}
 * com inclusão, edição e exclusão sem persistência.
 *
 * <p>Ideal para editar sub-listas dentro de formulários (ex.: endereços de um usuário,
 * dependentes, itens de pedido). Para telas completas com persistência, estenda
 * {@link BasePage}.
 *
 * <h3>Contrato da subclasse</h3>
 * <ul>
 *   <li>{@link #criarDataGrid()} — configura as colunas e retorna o {@link AppGrid}.</li>
 *   <li>{@link #criarFormulario(Object)} — cria o {@link Dialog} de edição/inclusão.
 *       O diálogo deve chamar {@link #adicionarItem}, {@link #substituirItem} ou
 *       {@link #atualizar} após salvar.</li>
 *   <li>{@link #tituloTabela()} e {@link #labelBotaoNovo()} — textos da toolbar.</li>
 * </ul>
 *
 * <h3>Inicialização</h3>
 * O layout é montado no primeiro {@link #onAttach}, garantindo que os campos da
 * subclasse já estejam inicializados (construtor da subclasse concluído).
 * {@link BasePage} sobrescreve esse comportamento usando {@code @PostConstruct}.
 *
 * @param <T> tipo do item gerenciado
 */
public abstract class DataEditor<T> extends VerticalLayout {

    /** Grid principal — disponível após a primeira montagem do componente. */
    protected AppGrid<T> grid;

    private final List<T> dados = new ArrayList<>();

    protected DataEditor() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    /**
     * Inicializa o editor com uma coleção de dados pré-carregada.
     * Os dados são aplicados ao grid assim que o componente for anexado ao DOM.
     *
     * <pre>{@code
     * var editor = new EnderecoEditor(usuario.getEnderecos());
     * }</pre>
     */
    protected DataEditor(java.util.Collection<T> dadosIniciais) {
        this();
        dados.addAll(dadosIniciais);
    }

    /**
     * Inicializa o editor consumindo um {@link java.util.stream.Stream}.
     * O stream é fechado automaticamente após a coleta.
     *
     * <pre>{@code
     * var editor = new EnderecoEditor(usuario.getEnderecos().stream()
     *         .filter(Endereco::isAtivo));
     * }</pre>
     */
    protected DataEditor(java.util.stream.Stream<T> dadosIniciais) {
        this();
        try (dadosIniciais) {
            dadosIniciais.forEach(dados::add);
        }
    }

    /**
     * Define os dados de forma fluente a partir de uma coleção.
     * Seguro chamar antes do attach — o grid será sincronizado quando montado.
     *
     * <pre>{@code
     * add(new EnderecoEditor().comDados(usuario.getEnderecos()));
     * }</pre>
     *
     * @return {@code this} para encadeamento
     */
    public DataEditor<T> comDados(java.util.Collection<T> itens) {
        setDados(new ArrayList<>(itens));
        return this;
    }

    /**
     * Define os dados de forma fluente a partir de um {@link java.util.stream.Stream}.
     * O stream é fechado automaticamente após a coleta.
     *
     * <pre>{@code
     * editor.comDados(repositorio.findAll().stream().filter(Endereco::isAtivo));
     * }</pre>
     *
     * @return {@code this} para encadeamento
     */
    public DataEditor<T> comDados(java.util.stream.Stream<T> stream) {
        try (stream) {
            setDados(stream.collect(java.util.stream.Collectors.toList()));
        }
        return this;
    }

    // ── Inicialização ─────────────────────────────────────────────────────────

    /**
     * Monta o layout na primeira vez que o componente é anexado ao DOM,
     * garantindo que os campos da subclasse já foram injetados.
     * {@link BasePage} desabilita este comportamento via {@code @PostConstruct}.
     */
    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        if (grid == null) {
            grid = criarDataGrid(criarObjectActions());
            construirLayout();
            sincronizarGrid(); // aplica dados pré-carregados antes do attach
        }
    }

    /**
     * Monta o {@link ObjectActions} padrão a partir dos métodos deste editor.
     * Sobrescreva para adicionar ou remover ações disponíveis.
     */
    protected ObjectActions<T> criarObjectActions() {
        return ObjectActions.<T>builder()
                .editar(this::abrirFormulario)
                .remover(this::confirmarRemocao)
                .build();
    }

    /**
     * Monta os componentes visuais. Sobrescreva em subclasses para customizar
     * o layout (ex.: {@link BasePage} adiciona cabeçalho e KPIs).
     */
    protected void construirLayout() {
        var card = new Div();
        card.addClassName("card");
        card.setSizeFull();
        setFlexGrow(1, card);
        card.add(criarToolbar(tituloTabela(), labelBotaoNovo()), grid);
        add(card);
    }

    // ── Métodos abstratos ──────────────────────────────────────────────────────

    /**
     * Cria e configura o {@link AppGrid} com as colunas necessárias.
     * Recebe as {@link ObjectActions} prontas para uso nos botões de ação.
     * Não deve realizar chamadas a serviços — apenas configuração de colunas.
     *
     * @param actions ações CRUD pré-configuradas pelo editor
     */
    protected abstract AppGrid<T> criarDataGrid(ObjectActions<T> actions);

    /**
     * Cria o {@link Dialog} de formulário para inclusão ({@code item = null}) ou edição.
     * O diálogo é responsável por chamar os helpers de memória ao salvar:
     * {@link #adicionarItem}, {@link #substituirItem} ou {@link #atualizar}.
     *
     * @param item {@code null} para inclusão, instância existente para edição
     */
    protected abstract Dialog criarFormulario(@Nullable T item);

    /** Rótulo exibido na toolbar acima da grid. */
    protected String tituloTabela() {
        return "Registros";
    }

    /** Texto do botão de inclusão. */
    protected String labelBotaoNovo() {
        return "Novo";
    }

    // ── Formulário ────────────────────────────────────────────────────────────

    /** Abre o formulário. Pode ser sobrescrito para customizar o fluxo de abertura. */
    protected void abrirFormulario(@Nullable T item) {
        criarFormulario(item).open();
    }

    // ── Exclusão ──────────────────────────────────────────────────────────────

    /**
     * Exibe diálogo de confirmação e, se confirmado, chama {@link #executarRemocao}.
     *
     * @param item          item a ser removido
     * @param nomeExibido   nome/descrição exibido na mensagem de confirmação
     */
    protected void confirmarRemocao(T item, String nomeExibido) {
        var dlg = new ConfirmDialog();
        dlg.setHeader("Confirmar remoção");
        dlg.setText("Deseja remover \"" + nomeExibido + "\"? Esta ação não poderá ser desfeita.");
        dlg.setCancelable(true);
        dlg.setConfirmText("Remover");
        dlg.setConfirmButtonTheme("error primary");
        dlg.addConfirmListener(e -> executarRemocao(item));
        dlg.open();
    }

    /**
     * Executa a remoção. Comportamento padrão: remove da lista em memória.
     * {@link BasePage} sobrescreve para persistir antes de remover.
     */
    protected void executarRemocao(T item) {
        dados.remove(item);
        sincronizarGrid();
    }

    // ── Helpers de memória ────────────────────────────────────────────────────

    /** Adiciona um novo item à lista em memória e atualiza o grid. */
    protected void adicionarItem(T item) {
        dados.add(item);
        sincronizarGrid();
    }

    /**
     * Substitui {@code antigo} por {@code novo} na lista em memória.
     * Se {@code antigo} não for encontrado, {@code novo} é adicionado ao final.
     */
    protected void substituirItem(T antigo, T novo) {
        int idx = dados.indexOf(antigo);
        if (idx >= 0) {
            dados.set(idx, novo);
        } else {
            dados.add(novo);
        }
        sincronizarGrid();
    }

    /** Recarrega o grid com o estado atual da lista em memória. */
    protected void atualizar() {
        sincronizarGrid();
    }

    /**
     * Substitui toda a lista em memória e atualiza o grid.
     * Usado por {@link BasePage} após recarregar dados do serviço.
     */
    protected void setDados(List<T> itens) {
        dados.clear();
        dados.addAll(itens);
        sincronizarGrid();
    }

    /**
     * Substitui toda a lista em memória a partir de um {@link java.util.stream.Stream}.
     * O stream é fechado automaticamente após a coleta.
     */
    protected void setDados(java.util.stream.Stream<T> stream) {
        try (stream) {
            setDados(stream.collect(java.util.stream.Collectors.toList()));
        }
    }

    /** Retorna visão imutável da lista em memória. */
    protected List<T> getDados() {
        return Collections.unmodifiableList(dados);
    }

    private void sincronizarGrid() {
        if (grid != null) {
            grid.setItems(new ArrayList<>(dados));
        }
    }

    // ── Helpers de UI ─────────────────────────────────────────────────────────

    /**
     * Cria a toolbar padrão com título e botão de inclusão.
     *
     * @param titulo    rótulo exibido à esquerda
     * @param labelNovo texto do botão de inclusão
     */
    protected HorizontalLayout criarToolbar(String titulo, String labelNovo) {
        var t = new Span(titulo);
        t.addClassName("card-title");

        var btnNovo = new Button(labelNovo, VaadinIcon.PLUS.create(),
                e -> abrirFormulario(null));
        btnNovo.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(t, btnNovo);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.expand(t);
        toolbar.addClassName("card-toolbar");
        return toolbar;
    }

    protected void notificarSucesso(String mensagem) {
        Notification.show(mensagem).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    protected void notificarErro(String mensagem) {
        Notification.show(mensagem).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

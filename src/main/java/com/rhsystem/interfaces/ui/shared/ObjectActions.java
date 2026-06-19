package com.rhsystem.interfaces.ui.shared;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Conjunto de ações CRUD disponíveis para um tipo de objeto {@code T}.
 *
 * <p>Centraliza os callbacks de operações (editar, remover, etc.) num único
 * objeto, evitando que {@link AppGrid} e {@link BasePage} precisem conhecer
 * cada callback individualmente.
 *
 * <p>Criado por {@link DataEditor} a partir de seus próprios métodos e passado
 * para {@code criarDataGrid(ObjectActions)} — a {@link AppGrid} usa as ações
 * sem saber quem as implementa.
 *
 * <pre>{@code
 * // DataEditor monta as actions automaticamente:
 * ObjectActions<Usuario> actions = ObjectActions.<Usuario>builder()
 *     .editar(this::abrirFormulario)
 *     .remover(this::confirmarRemocao)
 *     .build();
 *
 * // Grid consome sem conhecer a Page:
 * new Button("Editar", e -> actions.editar(usuario));
 * new Button("Remover", e -> actions.remover(usuario, usuario.getNome()));
 * }</pre>
 *
 * @param <T> tipo do objeto ao qual as ações se aplicam
 */
public final class ObjectActions<T> {

    private final Consumer<T> onEditar;
    private final BiConsumer<T, String> onRemover;

    private ObjectActions(Builder<T> builder) {
        this.onEditar  = builder.onEditar;
        this.onRemover = builder.onRemover;
    }

    // ── Execução ──────────────────────────────────────────────────────────────

    /** Dispara a ação de edição. No-op se não configurada. */
    public void editar(T item) {
        if (onEditar != null) onEditar.accept(item);
    }

    /**
     * Dispara a ação de remoção, passando o nome do item para exibição
     * no diálogo de confirmação. No-op se não configurada.
     *
     * @param item         item a ser removido
     * @param nomeExibido  texto exibido na mensagem de confirmação
     */
    public void remover(T item, String nomeExibido) {
        if (onRemover != null) onRemover.accept(item, nomeExibido);
    }

    // ── Consulta de disponibilidade ───────────────────────────────────────────

    public boolean podeEditar()  { return onEditar  != null; }
    public boolean podeRemover() { return onRemover != null; }

    // ── Builder ───────────────────────────────────────────────────────────────

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {

        private Consumer<T> onEditar;
        private BiConsumer<T, String> onRemover;

        private Builder() {}

        /** Define a ação de edição. */
        public Builder<T> editar(Consumer<T> acao) {
            this.onEditar = acao;
            return this;
        }

        /**
         * Define a ação de remoção.
         *
         * @param acao recebe o item e seu nome de exibição para o diálogo de confirmação
         */
        public Builder<T> remover(BiConsumer<T, String> acao) {
            this.onRemover = acao;
            return this;
        }

        public ObjectActions<T> build() {
            return new ObjectActions<>(this);
        }
    }
}

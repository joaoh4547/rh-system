package com.rhsystem.interfaces.ui.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Diálogo padrão que hospeda um {@link Form} e expõe ações de rodapé via
 * {@link FormDialogAction}, simplificando a criação de telas de cadastro/edição.
 *
 * <pre>{@code
 * UsuarioForm form = new UsuarioForm();
 * FormDialog<UsuarioModel> dialog = new FormDialog<>("Novo usuário", form)
 *     .width("680px")
 *     .actions(
 *         FormDialogAction.cancel(),
 *         FormDialogAction.primary("Salvar", () -> salvar(form)));
 * dialog.open(model);   // carrega o bean e abre
 * }</pre>
 *
 * @param <T> tipo do bean editado pelo formulário
 */
public class FormDialog<T> extends Dialog {

    private final Form<T> form;
    private final Button botaoMaximizar;
    private String larguraNormal = "680px";
    private String alturaNormal = null;
    private boolean maximizado = false;

    public FormDialog(String title, Form<T> form) {
        this.form = form;
        setHeaderTitle(title);
        setModal(true);
        setDraggable(true);
        setResizable(true);
        setWidth(larguraNormal);
        add(form);

        botaoMaximizar = botaoIcone(VaadinIcon.EXPAND_FULL, "Maximizar", this::alternarMaximizar);
        Button botaoFechar = botaoIcone(VaadinIcon.CLOSE, "Fechar", this::close);
        getHeader().add(botaoMaximizar, botaoFechar);
    }

    /** Cria um botão somente-ícone para o cabeçalho do diálogo. */
    private Button botaoIcone(VaadinIcon icone, String titulo, Runnable acao) {
        Button b = new Button(icone.create(), e -> acao.run());
        b.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        b.getElement().setAttribute("title", titulo);
        b.getElement().setAttribute("aria-label", titulo);
        b.addClassName("dialog-header-button");
        return b;
    }

    /** Alterna entre tamanho normal e tela cheia (maximizar/restaurar). */
    private void alternarMaximizar() {
        maximizado = !maximizado;
        if (maximizado) {
            // O atributo "theme" é propagado para o <vaadin-dialog-overlay>;
            // o CSS força posição/tamanho com !important, ignorando o offset
            // deixado pelo arraste (draggable).
            getElement().getThemeList().add("maximized");
            setWidth("100vw");
            setHeight("100vh");
            atualizarBotaoMaximizar(VaadinIcon.COMPRESS_SQUARE, "Restaurar");
        } else {
            getElement().getThemeList().remove("maximized");
            setWidth(larguraNormal);
            setHeight(alturaNormal);
            atualizarBotaoMaximizar(VaadinIcon.EXPAND_FULL, "Maximizar");
        }
        limparPosicaoArraste();
    }

    /**
     * Remove a posição inline (top/left) que o arraste grava na parte interna
     * do overlay, evitando que o diálogo maximize deslocado para um lado.
     */
    private void limparPosicaoArraste() {
        getElement().executeJs(
                "const dlg = this;" +
                "let ov = (dlg.$ && dlg.$.overlay)" +
                " || (dlg.shadowRoot && dlg.shadowRoot.querySelector('vaadin-dialog-overlay'))" +
                " || document.querySelector('vaadin-dialog-overlay');" +
                "if (ov) {" +
                " const inner = (ov.$ && ov.$.overlay)" +
                "   || (ov.shadowRoot && ov.shadowRoot.querySelector('[part=\"overlay\"]'));" +
                " [ov, inner].forEach(function(el){ if(!el) return;" +
                "  el.style.top=''; el.style.left=''; el.style.right='';" +
                "  el.style.bottom=''; el.style.position=''; el.style.transform=''; });" +
                "}");
    }

    private void atualizarBotaoMaximizar(VaadinIcon icone, String titulo) {
        botaoMaximizar.setIcon(icone.create());
        botaoMaximizar.getElement().setAttribute("title", titulo);
        botaoMaximizar.getElement().setAttribute("aria-label", titulo);
    }

    public Form<T> getForm() {
        return form;
    }

    public T getBean() {
        return form.getBean();
    }

    public FormDialog<T> width(String width) {
        this.larguraNormal = width;
        if (!maximizado) {
            setWidth(width);
        }
        return this;
    }

    public FormDialog<T> addAction(FormDialogAction action) {
        getFooter().add(action.build(this::close));
        return this;
    }

    public FormDialog<T> actions(FormDialogAction... actions) {
        for (FormDialogAction a : actions) {
            addAction(a);
        }
        return this;
    }

    /** Carrega o bean no formulário e abre o diálogo. */
    public void open(T bean) {
        form.setBean(bean);
        open();
    }
}

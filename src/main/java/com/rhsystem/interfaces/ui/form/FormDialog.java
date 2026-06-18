package com.rhsystem.interfaces.ui.form;

import com.vaadin.flow.component.dialog.Dialog;

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

    public FormDialog(String title, Form<T> form) {
        this.form = form;
        setHeaderTitle(title);
        setModal(true);
        setDraggable(true);
        setWidth("680px");
        add(form);
    }

    public Form<T> getForm() {
        return form;
    }

    public T getBean() {
        return form.getBean();
    }

    public FormDialog<T> width(String width) {
        setWidth(width);
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

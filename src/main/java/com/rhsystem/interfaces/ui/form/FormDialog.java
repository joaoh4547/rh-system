package com.rhsystem.interfaces.ui.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Standard dialog that hosts a {@link Form} and exposes footer actions via
 * {@link FormDialogAction}, simplifying the creation of add/edit screens.
 *
 * @param <T> type of the bean edited by the form
 */
public class FormDialog<T> extends Dialog {

    private final Form<T> form;
    private final Button maximizeButton;
    private String normalWidth = "680px";
    private String normalHeight = null;
    private boolean maximized = false;

    public FormDialog(String title, Form<T> form) {
        this.form = form;
        setHeaderTitle(title);
        setDraggable(true);
        setResizable(true);
        setWidth(normalWidth);
        add(form);

        maximizeButton = iconButton(VaadinIcon.EXPAND_FULL, "Maximizar", this::toggleMaximize);
        Button closeButton = iconButton(VaadinIcon.CLOSE, "Fechar", this::close);
        getHeader().add(maximizeButton, closeButton);
    }

    private Button iconButton(VaadinIcon icon, String title, Runnable action) {
        Button b = new Button(icon.create(), e -> action.run());
        b.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        b.getElement().setAttribute("title", title);
        b.getElement().setAttribute("aria-label", title);
        b.addClassName("dialog-header-button");
        return b;
    }

    private void toggleMaximize() {
        maximized = !maximized;
        if (maximized) {
            getElement().getThemeList().add("maximized");
            setWidth("100vw");
            setHeight("100vh");
            updateMaximizeButton(VaadinIcon.COMPRESS_SQUARE, "Restaurar");
        } else {
            getElement().getThemeList().remove("maximized");
            setWidth(normalWidth);
            setHeight(normalHeight);
            updateMaximizeButton(VaadinIcon.EXPAND_FULL, "Maximizar");
        }
        clearDragPosition();
    }

    private void clearDragPosition() {
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

    private void updateMaximizeButton(VaadinIcon icon, String title) {
        maximizeButton.setIcon(icon.create());
        maximizeButton.getElement().setAttribute("title", title);
        maximizeButton.getElement().setAttribute("aria-label", title);
    }

    public Form<T> getForm() {
        return form;
    }

    public T getBean() {
        return form.getBean();
    }

    public FormDialog<T> width(String width) {
        this.normalWidth = width;
        if (!maximized) {
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

    /** Loads the bean into the form and opens the dialog. */
    public void open(T bean) {
        form.setBean(bean);
        open();
    }
}

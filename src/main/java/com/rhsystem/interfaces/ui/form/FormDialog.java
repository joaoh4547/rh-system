package com.rhsystem.interfaces.ui.form;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.shared.Tooltip;
import lombok.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Standard dialog that hosts a {@link Form} and exposes footer actions via
 * {@link FormDialogAction}, simplifying the creation of add/edit screens.
 *
 * @param <T> type of the bean edited by the form
 */
public class FormDialog<T> extends Dialog {

    @Getter
    private final Form<T> form;
    private final Button maximizeButton;
    private String normalWidth = "680px";
    private boolean maximized = false;

    private Map<Button, Tooltip> TOOLTIP_MAP = new LinkedHashMap<>();

    public FormDialog(String title, Form<T> form) {
        this.form = form;
        addThemeName("form-dialog");
        final String header = getTranslation(title);

        setAriaLabel(header);


        setDraggable(true);
        setResizable(true);
        setWidth(normalWidth);
        add(form);

        Span titleLabel = new Span(header);
        titleLabel.addClassName("form-dialog-title");
        titleLabel.addClassName("draggable");

        Div spacer = new Div();
        spacer.addClassName("draggable");
        spacer.getStyle().set("flex-grow", "1");

        maximizeButton = iconButton(VaadinIcon.EXPAND_FULL, "Maximizar", this::toggleMaximize);
        Button closeButton = iconButton(VaadinIcon.CLOSE, "Fechar", this::close);
        getHeader().add(titleLabel, spacer, maximizeButton, closeButton);

        if (startMaximized()) {
            maximize();
        }
    }

    protected boolean startMaximized() {
        return false;
    }

    private Button iconButton(VaadinIcon icon, String title, Runnable action) {
        Button b = new Button(icon.create(), e -> action.run());
        b.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_SMALL);
        var tooltip = Tooltip.forComponent(b);
        tooltip.setText(title);
        TOOLTIP_MAP.put(b, tooltip);

        b.getElement().setAttribute("aria-label", title);
        b.addClassName("dialog-header-button");
        return b;
    }

    protected void toggleMaximize() {
        maximized = !maximized;
        if (maximized) {
            getElement().getThemeList().add("maximized");
            setWidth("100vw");
            setHeight("100vh");
            updateMaximizeButton(VaadinIcon.COMPRESS_SQUARE, "Restaurar");
        } else {
            getElement().getThemeList().remove("maximized");
            setWidth(normalWidth);
            updateMaximizeButton(VaadinIcon.EXPAND_FULL, "Maximizar");
        }
        clearDragPosition();
    }

    protected void maximize() {
        this.maximized = true;
        getElement().getThemeList().add("maximized");
        setWidth("100vw");
        setHeight("100vh");
        updateMaximizeButton(VaadinIcon.COMPRESS_SQUARE, "Restaurar");
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
        var ref = TOOLTIP_MAP.get(maximizeButton);
        if (ref != null) {
           ref.setText(title);
        }
        maximizeButton.getElement().setAttribute("aria-label", title);
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

    /**
     * Loads the bean into the form and opens the dialog.
     */
    public void open(T bean) {
        form.setBean(bean);
        open();
    }
}

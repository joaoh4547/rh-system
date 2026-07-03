package com.rhsystem.interfaces.ui.form;

import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Descreve um botão de rodapé de um {@link FormDialog}: texto, ícone, variantes
 * de tema, comportamento ao clicar e se fecha o diálogo.
 *
 * <pre>{@code
 * FormDialogAction.primary("Salvar", this::salvar);
 * FormDialogAction.cancel();
 * FormDialogAction.danger("Excluir", this::excluir);
 * }</pre>
 */
public final class FormDialogAction implements Serializable {

    private final String text;
    private final Runnable handler;
    private final List<ButtonVariant> variants = new ArrayList<>();
    private Component icon;
    private boolean closeOnClick;
    private boolean closeOnlyIfNoError = false;

    public FormDialogAction(String text, Runnable handler) {
        this.text = text;
        this.handler = handler;
    }

    public FormDialogAction icon(VaadinIcon icon) {
        this.icon = icon.create();
        return this;
    }

    public FormDialogAction icon(LucideIcon icon) {
        this.icon = icon;
        return this;
    }

    public FormDialogAction variants(ButtonVariant... variants) {
        this.variants.addAll(List.of(variants));
        return this;
    }

    /** Fecha o diálogo após executar o handler (mesmo que o handler lance exceção, não fecha). */
    public FormDialogAction closeOnClick() {
        this.closeOnClick = true;
        this.closeOnlyIfNoError = true;
        return this;
    }

    /* ===================== Fábricas convenientes ===================== */

    public static FormDialogAction primary(String text, Runnable handler) {
        return new FormDialogAction(text, handler).variants(ButtonVariant.LUMO_PRIMARY);
    }

    public static FormDialogAction tertiary(String text, Runnable handler) {
        return new FormDialogAction(text, handler).variants(ButtonVariant.LUMO_TERTIARY);
    }

    public static FormDialogAction danger(String text, Runnable handler) {
        return new FormDialogAction(text, handler)
                .variants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
    }

    /** Ação "Cancelar" padrão: fecha o diálogo sem efeito. */
    public static FormDialogAction cancel() {
        return tertiary("Cancelar", null).closeOnClick();
    }

    public static FormDialogAction cancel(String text) {
        return tertiary(text, null).closeOnClick();
    }

    /** Constrói o botão; {@code closeAction} é fornecido pelo diálogo. */
    Button build(Runnable closeAction) {
        Button button = (icon != null) ? new Button(text, icon) : new Button(text);
        if (!variants.isEmpty()) {
            button.addThemeVariants(variants.toArray(new ButtonVariant[0]));
        }
        button.addClickListener(e -> {
            boolean erro = false;
            if (handler != null) {
                try {
                    handler.run();
                } catch (RuntimeException ex) {
                    erro = true;
                    throw ex;
                } finally {
                    if (closeOnClick && closeAction != null && (!closeOnlyIfNoError || !erro)) {
                        closeAction.run();
                    }
                }
            } else if (closeOnClick && closeAction != null) {
                closeAction.run();
            }
        });
        return button;
    }
}

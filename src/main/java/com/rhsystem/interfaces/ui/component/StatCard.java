package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * Card de métrica (KPI) no estilo dashboard: ícone, valor e rótulo,
 * com uma cor de destaque ("accent") aplicada via classe utilitária.
 */
public class StatCard extends Div {

    /** Cores de destaque disponíveis (mapeadas no styles.css). */
    public enum Accent { PRIMARY, SUCCESS, WARNING, DANGER }

    public StatCard(String rotulo, long valor, VaadinIcon icone, Accent accent) {
        addClassName("stat-card");
        addClassName("accent-" + accent.name().toLowerCase());

        Icon ic = icone.create();
        ic.addClassName("stat-icon");

        Span valorSpan = new Span(String.valueOf(valor));
        valorSpan.addClassName("stat-value");

        Span rotuloSpan = new Span(rotulo);
        rotuloSpan.addClassName("stat-label");

        Div textos = new Div(valorSpan, rotuloSpan);
        textos.addClassName("stat-texts");

        add(ic, textos);
    }
}

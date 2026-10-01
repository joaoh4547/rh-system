package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;

/**
 * KPI metric card in dashboard style: icon, value and label
 * with an accent color applied via a utility class.
 */
public class StatCard extends Div {

    /** Available accent colours (mapped in styles.css). */
    public enum Accent { PRIMARY, SUCCESS, WARNING, DANGER }

    public StatCard(String label, Number value, VaadinIcon icon, Accent accent) {
        this(label, String.valueOf(value), icon, accent);
    }

    public StatCard(String label, String value, VaadinIcon icon, Accent accent) {
        addClassName("stat-card");
        addClassName("accent-" + accent.name().toLowerCase());

        Icon ic = icon.create();
        ic.addClassName("stat-icon");

        Span valueSpan = new Span(value);
        valueSpan.addClassName("stat-value");

        Span labelSpan = new Span(label);
        labelSpan.addClassName("stat-label");

        Div texts = new Div(valueSpan, labelSpan);
        texts.addClassName("stat-texts");

        add(ic, texts);
    }
}

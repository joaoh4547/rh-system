package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Text field that applies a mask automatically as the user types.
 * Supports CPF (000.000.000-00) and RG (00.000.000-0).
 */
public class DocumentField extends TextField {

    public enum Type { CPF, RG }

    private final Type tipo;

    public DocumentField(String label, Type tipo) {
        super(label);
        this.tipo = tipo;
        setValueChangeMode(ValueChangeMode.EAGER);
        setAllowedCharPattern("[0-9.\\-]");
        setPlaceholder(tipo == Type.CPF ? "000.000.000-00" : "00.000.000-0");
        addValueChangeListener(e -> {
            String formatted = format(e.getValue());
            if (!formatted.equals(getValue())) {
                setValue(formatted);
            }
        });
    }

    /** Returns only the typed digits (without mask). */
    public String getDigits() {
        return getValue() == null ? "" : getValue().replaceAll("\\D", "");
    }

    public void setDigits(String digits) {
        setValue(format(digits));
    }

    private String format(String value) {
        String d = value == null ? "" : value.replaceAll("\\D", "");
        int max = tipo == Type.CPF ? 11 : 9;
        if (d.length() > max) {
            d = d.substring(0, max);
        }
        return tipo == Type.CPF ? formatCpf(d) : formatRg(d);
    }

    private String formatCpf(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 3 || i == 6) {
                sb.append('.');
            } else if (i == 9) {
                sb.append('-');
            }
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }

    private String formatRg(String d) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < d.length(); i++) {
            if (i == 2 || i == 5) {
                sb.append('.');
            } else if (i == 8) {
                sb.append('-');
            }
            sb.append(d.charAt(i));
        }
        return sb.toString();
    }
}

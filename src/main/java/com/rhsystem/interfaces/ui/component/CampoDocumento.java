package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Campo de texto que aplica máscara automaticamente enquanto o usuário digita.
 * Suporta CPF (000.000.000-00) e RG (00.000.000-0).
 */
public class CampoDocumento extends TextField {

    public enum Tipo { CPF, RG }

    private final Tipo tipo;

    public CampoDocumento(String label, Tipo tipo) {
        super(label);
        this.tipo = tipo;
        setValueChangeMode(ValueChangeMode.EAGER);
        setAllowedCharPattern("[0-9.\\-]");
        setPlaceholder(tipo == Tipo.CPF ? "000.000.000-00" : "00.000.000-0");
        addValueChangeListener(e -> {
            String formatado = formatar(e.getValue());
            if (!formatado.equals(getValue())) {
                setValue(formatado);
            }
        });
    }

    /** Retorna apenas os dígitos digitados (sem máscara). */
    public String getDigitos() {
        return getValue() == null ? "" : getValue().replaceAll("\\D", "");
    }

    public void setDigitos(String digitos) {
        setValue(formatar(digitos));
    }

    private String formatar(String valor) {
        String d = valor == null ? "" : valor.replaceAll("\\D", "");
        int max = tipo == Tipo.CPF ? 11 : 9;
        if (d.length() > max) {
            d = d.substring(0, max);
        }
        return tipo == Tipo.CPF ? formatarCpf(d) : formatarRg(d);
    }

    private String formatarCpf(String d) {
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

    private String formatarRg(String d) {
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

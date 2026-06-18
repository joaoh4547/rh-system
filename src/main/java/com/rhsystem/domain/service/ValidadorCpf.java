package com.rhsystem.domain.service;

/**
 * Serviço de domínio para validação de CPF (algoritmo dos dígitos verificadores).
 */
public final class ValidadorCpf {

    private ValidadorCpf() {
    }

    /** Remove tudo que não for dígito. */
    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public static boolean isValido(String cpf) {
        String c = apenasDigitos(cpf);
        if (c.length() != 11) {
            return false;
        }
        // Rejeita sequências de dígitos iguais (ex: 00000000000)
        if (c.chars().distinct().count() == 1) {
            return false;
        }
        try {
            int d1 = calcularDigito(c, 9, 10);
            int d2 = calcularDigito(c, 10, 11);
            return d1 == (c.charAt(9) - '0') && d2 == (c.charAt(10) - '0');
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static int calcularDigito(String cpf, int qtd, int pesoInicial) {
        int soma = 0;
        int peso = pesoInicial;
        for (int i = 0; i < qtd; i++) {
            soma += (cpf.charAt(i) - '0') * peso;
            peso--;
        }
        int resto = soma % 11;
        return (resto < 2) ? 0 : 11 - resto;
    }
}

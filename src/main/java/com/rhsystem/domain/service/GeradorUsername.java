package com.rhsystem.domain.service;

import java.text.Normalizer;
import java.util.function.Predicate;

/**
 * Serviço de domínio que gera o username a partir de nome e sobrenome.
 * Padrão: "nome.sobrenome" (sem acentos, minúsculo). Se já existir,
 * acrescenta um número incremental: nome.sobrenome1, nome.sobrenome2, ...
 */
public final class GeradorUsername {

    private GeradorUsername() {
    }

    /**
     * @param nome      primeiro nome
     * @param sobrenome sobrenome
     * @param jaExiste  predicado que indica se um username já está em uso
     * @return username único conforme o predicado informado
     */
    public static String gerar(String nome, String sobrenome, Predicate<String> jaExiste) {
        String base = normalizar(nome) + "." + normalizar(sobrenome);
        if (!jaExiste.test(base)) {
            return base;
        }
        int sufixo = 1;
        String candidato = base + sufixo;
        while (jaExiste.test(candidato)) {
            sufixo++;
            candidato = base + sufixo;
        }
        return candidato;
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // mantém apenas letras e dígitos, em minúsculo
        return semAcento.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}

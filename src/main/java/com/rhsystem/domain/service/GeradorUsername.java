package com.rhsystem.domain.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Serviço de domínio que gera o username a partir de nome e sobrenome.
 *
 * <p>Regra: {@code primeiroNome.ultimoSobrenome} (sem acentos, minúsculo).</p>
 * <ul>
 *   <li>Se o <b>nome</b> tem 2+ palavras, usa a 1ª e a última palavra do nome
 *       (ex.: "João Henrique" → {@code joao.henrique}).</li>
 *   <li>Se o nome tem 1 palavra, usa a 1ª do nome e a última do sobrenome
 *       (ex.: "João" + "Teixeira de Jesus" → {@code joao.jesus}).</li>
 * </ul>
 * Conectivos ("de", "da", "do", "dos", "das", "e"...) são ignorados ao escolher
 * a última parte. Em caso de colisão, acrescenta sufixo numérico incremental.
 */
public final class GeradorUsername {

    private static final Set<String> CONECTIVOS =
            Set.of("de", "da", "do", "dos", "das", "e", "di", "du", "del", "la", "las", "los");

    private GeradorUsername() {
    }

    /**
     * @param nome      primeiro nome (pode conter mais de uma palavra)
     * @param sobrenome sobrenome
     * @param jaExiste  predicado que indica se um username já está em uso
     * @return username único conforme o predicado informado
     */
    public static String gerar(String nome, String sobrenome, Predicate<String> jaExiste) {
        List<String> nomeTokens = tokenizar(nome);
        List<String> sobreTokens = tokenizar(sobrenome);

        String primeiro = nomeTokens.isEmpty() ? "" : nomeTokens.get(0);
        String ultimo;
        if (nomeTokens.size() >= 2) {
            ultimo = ultimoSignificativo(nomeTokens);
        } else {
            ultimo = ultimoSignificativo(sobreTokens);
        }

        String base;
        if (primeiro.isEmpty()) {
            base = ultimo;
        } else if (ultimo.isEmpty()) {
            base = primeiro;
        } else {
            base = primeiro + "." + ultimo;
        }
        if (base.isEmpty()) {
            base = "usuario";
        }

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

    /** Quebra em palavras normalizadas (sem acento, minúsculo, só letras/dígitos), sem vazios. */
    private static List<String> tokenizar(String valor) {
        List<String> tokens = new ArrayList<>();
        if (valor == null) {
            return tokens;
        }
        for (String parte : valor.trim().split("\\s+")) {
            String norm = normalizar(parte);
            if (!norm.isEmpty()) {
                tokens.add(norm);
            }
        }
        return tokens;
    }

    /** Última palavra que não seja conectivo; se só houver conectivos, a última. */
    private static String ultimoSignificativo(List<String> tokens) {
        if (tokens.isEmpty()) {
            return "";
        }
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (!CONECTIVOS.contains(tokens.get(i))) {
                return tokens.get(i);
            }
        }
        return tokens.get(tokens.size() - 1);
    }

    private static String normalizar(String valor) {
        if (valor == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(valor.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}

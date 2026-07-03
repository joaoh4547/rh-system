package com.rhsystem.domain.service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Domain service that generates a username from first and last name.
 *
 * <p>Rule: {@code firstName.lastName} (no accents, lowercase).</p>
 * <ul>
 *   <li>If <b>firstName</b> has 2+ words, uses the 1st and last word of the name
 *       (e.g. "João Henrique" → {@code joao.henrique}).</li>
 *   <li>If the name has 1 word, uses the 1st of the name and the last of the last name
 *       (e.g. "João" + "Teixeira de Jesus" → {@code joao.jesus}).</li>
 * </ul>
 * Connectives ("de", "da", "do", ...) are skipped when choosing the last part.
 * In case of collision, appends an incremental numeric suffix.
 */
public final class UsernameGenerator {

    private static final Set<String> CONNECTIVES =
            Set.of("de", "da", "do", "dos", "das", "e", "di", "du", "del", "la", "las", "los");

    private UsernameGenerator() {
    }

    /**
     * @param firstName    first name (may contain multiple words)
     * @param lastName     last name
     * @param alreadyExists predicate indicating whether a username is already taken
     * @return a unique username according to the given predicate
     */
    public static String generate(String firstName, String lastName, Predicate<String> alreadyExists) {
        List<String> firstNameTokens = tokenize(firstName);
        List<String> lastNameTokens = tokenize(lastName);

        String first = firstNameTokens.isEmpty() ? "" : firstNameTokens.getFirst();
        String last;
        if (firstNameTokens.size() >= 2) {
            last = lastSignificant(firstNameTokens);
        } else {
            last = lastSignificant(lastNameTokens);
        }

        String base;
        if (first.isEmpty()) {
            base = last;
        } else if (last.isEmpty()) {
            base = first;
        } else {
            base = first + "." + last;
        }
        if (base.isEmpty()) {
            base = "user";
        }

        if (!alreadyExists.test(base)) {
            return base;
        }
        int suffix = 1;
        String candidate = base + suffix;
        while (alreadyExists.test(candidate)) {
            suffix++;
            candidate = base + suffix;
        }
        return candidate;
    }

    /** Splits into normalized tokens (no accents, lowercase, letters/digits only), no blanks. */
    private static List<String> tokenize(String value) {
        List<String> tokens = new ArrayList<>();
        if (value == null) {
            return tokens;
        }
        for (String part : value.trim().split("\\s+")) {
            String norm = normalize(part);
            if (!norm.isEmpty()) {
                tokens.add(norm);
            }
        }
        return tokens;
    }

    /** Last word that is not a connective; if only connectives exist, returns the last one. */
    private static String lastSignificant(List<String> tokens) {
        if (tokens.isEmpty()) {
            return "";
        }
        for (int i = tokens.size() - 1; i >= 0; i--) {
            if (!CONNECTIVES.contains(tokens.get(i))) {
                return tokens.get(i);
            }
        }
        return tokens.getLast();
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        String withoutAccents = Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return withoutAccents.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}

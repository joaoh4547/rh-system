package com.rhsystem.infrastructure.i18n;

import com.vaadin.flow.i18n.I18NProvider;
import java.util.List;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;

@Component
public class TranslationProvider implements I18NProvider {

    public static final Locale PT_BR = new Locale("pt", "BR");
    private static final List<Locale> SUPPORTED = List.of(PT_BR, Locale.ENGLISH);

    private final MessageSource messageSource;

    public TranslationProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public List<Locale> getProvidedLocales() {
        return SUPPORTED;
    }

    @Override
    public String getTranslation(String key, Locale locale, Object... params) {
        try {
            return messageSource.getMessage(key, params.length == 0 ? null : params, locale);
        } catch (NoSuchMessageException e) {
            return "!" + key;
        }
    }
}

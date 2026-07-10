package com.rhsystem.application.parameter;

import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
import com.rhsystem.domain.model.security.ValueDecoder;
import com.rhsystem.utils.NumberParser;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class DefaultParameterValueConverter implements ParameterValueConverter {


    private final ValueDecoder valueDecoder;

    @SuppressWarnings("unchecked")
    public <T> T convert(Parameter parameter) {
        return switch (parameter.getType()) {
            case TEXT -> (T) parameter.getValue();
            case NUMBER -> (T) NumberParser.parse(parameter.getValue());
            case SECRET -> (T) valueDecoder.decode(parameter.getValue());
            case BOOLEAN -> (T) castBoolean(parameter.getValue());
            case DATE -> (T) LocalDate.parse(parameter.getValue());
        };
    }

    private Boolean castBoolean(String value) {
        return Boolean.parseBoolean(value);
    }
}

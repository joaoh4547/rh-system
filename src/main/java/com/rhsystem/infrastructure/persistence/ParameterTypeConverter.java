package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.parameters.ParameterType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ParameterTypeConverter implements AttributeConverter<ParameterType, String> {

    @Override
    public String convertToDatabaseColumn(ParameterType type) {
        if (isNull(type)) {
            return null;
        }
        return type.getCode();
    }

    @Override
    public ParameterType convertToEntityAttribute(String code) {
        if (isNull(code)) {
            return null;
        }
        return ParameterType.fromCode(code);
    }

    private boolean isNull(Object target) {
        return target == null;
    }
}

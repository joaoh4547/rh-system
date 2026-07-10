package com.rhsystem.domain.model.parameters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.math.NumberUtils;

@AllArgsConstructor
@Getter
public enum ParameterType {


    TEXT("text"),
    NUMBER("number"),
    SECRET("secret"),
    BOOLEAN("boolean"),
    DATE("date");


    private final String code;

    public static ParameterType fromCode(String code) {
        for (ParameterType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid parameter type code: " + code);
    }

}

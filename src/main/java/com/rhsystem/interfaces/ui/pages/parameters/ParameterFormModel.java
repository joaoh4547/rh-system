package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterType;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
import com.rhsystem.domain.model.security.ValueDecoder;
import com.rhsystem.utils.SpringContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParameterFormModel{


    private String name;
    private ParameterType type;
    private Object value;





    public static ParameterFormModel of(Parameter target, ParameterValueConverter converter) {
        return new ParameterFormModel(target.getName(), target.getType(), fieldValue(target, converter));
    }

    /**
     * Converts the raw String value to the type expected by the bound field
     * (NumberField requires Double, Checkbox Boolean, DatePicker LocalDate).
     */
    private static Object fieldValue(Parameter target, ParameterValueConverter converter) {
        Object converted = converter.convert(target);
        if (target.getType() == ParameterType.NUMBER && converted instanceof Number number) {
            return number.doubleValue();
        }
        return converted;
    }

}

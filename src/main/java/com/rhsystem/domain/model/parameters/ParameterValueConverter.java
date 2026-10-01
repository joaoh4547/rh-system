package com.rhsystem.domain.model.parameters;

public interface ParameterValueConverter {

    <T> T convert(Parameter parameter);


}

package com.rhsystem.domain.model.parameters;

import com.rhsystem.utils.NumberParser;

public  interface ParameterValueConverter {

    <T> T convert(Parameter parameter);


}

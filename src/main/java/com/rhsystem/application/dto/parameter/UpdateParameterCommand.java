package com.rhsystem.application.dto.parameter;

import com.rhsystem.domain.model.parameters.AppParameter;

public record UpdateParameterCommand(AppParameter parameter, String name, String value) {
}

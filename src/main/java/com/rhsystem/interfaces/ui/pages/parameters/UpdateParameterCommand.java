package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.domain.model.parameters.AppParameter;

public record UpdateParameterCommand(AppParameter parameter, String name, String value) {
}

package com.rhsystem.application.dto.grupo;

import com.rhsystem.domain.model.Functionality;

import java.util.Collection;

public record UpdateGroupCommand(Long id, String name, String description,
                                 Collection<Functionality> functionalities) {
}

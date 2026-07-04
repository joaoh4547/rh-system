package com.rhsystem.application.dto.group;

import com.rhsystem.domain.model.Functionality;

import java.util.Collection;

public record UpdateGroupCommand(Long id, String name, String description, boolean active, boolean admin,
                                 Collection<Functionality> functionalities) {
}

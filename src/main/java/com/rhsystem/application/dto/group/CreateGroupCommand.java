package com.rhsystem.application.dto.group;

import com.rhsystem.domain.model.Functionality;

import java.util.Collection;

public record CreateGroupCommand(String name, String description, Collection<Functionality> functionalities) {
}

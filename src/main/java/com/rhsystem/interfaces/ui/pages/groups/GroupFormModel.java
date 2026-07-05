package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.grupo.Group;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
public class GroupFormModel {

    private String name;
    private String description;
    private boolean active = true;
    private boolean admin;

    private Set<Functionality> functionalities = new HashSet<>();


    public static GroupFormModel of(Group group) {
        GroupFormModel model = new GroupFormModel();
        model.setName(group.getName());
        model.setDescription(group.getDescription());
        model.setActive(group.isActive());
        model.setAdmin(group.isAdmin());
        model.setFunctionalities(new HashSet<>(group.getFunctionalities()));
        return model;
    }
}

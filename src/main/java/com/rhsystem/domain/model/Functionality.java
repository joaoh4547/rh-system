package com.rhsystem.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.*;

@Getter
@AllArgsConstructor
public enum Functionality {

    // Usuários
    CREATE_USER(Category.USER,"create-user.functionality"),
    VIEW_USER(Category.USER, "view-user.functionality"),
    DELETE_USER(Category.USER, "delete-user.functionality"),

    // Grupos
    CREATE_GROUP(Category.GROUP, "create-group.functionality"),
    VIEW_GROUP(Category.GROUP, "view-group.functionality"),
    DELETE_GROUP(Category.GROUP, "delete-group.functionality");

    private final Category category;
    private final String label;

    public String asRole(){
        return String.format("ROLE_%s", name());
    }


    public static Map<Category, Collection<Functionality>> getFunctionalityByCategory() {
        Map<Category, Collection<Functionality>> functionalityByCategory = new LinkedHashMap<>();
        for (Functionality functionality : values()) {
            functionalityByCategory.computeIfAbsent(functionality.getCategory(), k -> new ArrayList<>()).add(functionality);
        }
        return functionalityByCategory;
    }

    @AllArgsConstructor
    @Getter
    public enum Category {
        USER("functionality.category.USER"),
        GROUP("functionality.category.GROUP");

        private final String label;
    }
}




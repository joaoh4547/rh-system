package com.rhsystem.domain.model;

import lombok.Getter;

import java.util.*;

@Getter
public enum Functionality {

    // Usuários
    CREATE_USER(Category.USER),
    VIEW_USER(Category.USER),
    DELETE_USER(Category.USER),

    // Grupos
    CREATE_GROUP(Category.GROUP),
    VIEW_GROUP(Category.GROUP),
    DELETE_GROUP(Category.GROUP);

    private final Category category;

    Functionality(final Category category) {
        this.category = category;
    }


    public static Map<Category, Collection<Functionality>> getFunctionalityByCategory() {
        Map<Category, Collection<Functionality>> functionalityByCategory = new LinkedHashMap<>();
        for (Functionality functionality : values()) {
            functionalityByCategory.computeIfAbsent(functionality.getCategory(), k -> new ArrayList<>()).add(functionality);
        }
        return functionalityByCategory;
    }

    public enum Category {
        USER,
        GROUP
    }
}




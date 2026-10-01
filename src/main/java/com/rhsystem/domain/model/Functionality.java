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
    DELETE_GROUP(Category.GROUP, "delete-group.functionality"),
    ENABLE_DISABLE_GROUP(Category.GROUP, "enable-disable-group.functionality"),

    // Sistema
    MANAGE_PARAMETERS(Category.SYSTEM, "manage-parameters.functionality"),
    MANAGE_CACHE(Category.SYSTEM, "manage-cache.functionality");

    /**
     * Compile-time constants with the role names (= enum names), for use in
     * {@code @RolesAllowed} on views and {@code @PreAuthorize} on use cases (annotations need constants;
     * {@code Functionality.X.name()} is not one). Spring adds the {@code ROLE_}
     * prefix, matching {@link #asRole()}. {@code FunctionalityTest} guarantees
     * every constant matches an enum value.
     */
    public static final class Roles {
        public static final String CREATE_USER = "CREATE_USER";
        public static final String VIEW_USER = "VIEW_USER";
        public static final String DELETE_USER = "DELETE_USER";
        public static final String CREATE_GROUP = "CREATE_GROUP";
        public static final String VIEW_GROUP = "VIEW_GROUP";
        public static final String DELETE_GROUP = "DELETE_GROUP";
        public static final String ENABLE_DISABLE_GROUP = "ENABLE_DISABLE_GROUP";
        public static final String MANAGE_PARAMETERS = "MANAGE_PARAMETERS";
        public static final String MANAGE_CACHE = "MANAGE_CACHE";

        private Roles() {
        }
    }

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
        GROUP("functionality.category.GROUP"),
        SYSTEM("functionality.category.SYSTEM");

        private final String label;
    }
}




package com.rhsystem.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class FunctionalityTest {

    @Test
    void asRolePrefixesEnumName() {
        assertEquals("ROLE_CREATE_USER", Functionality.CREATE_USER.asRole());
        assertEquals("ROLE_ENABLE_DISABLE_GROUP", Functionality.ENABLE_DISABLE_GROUP.asRole());
    }

    @Test
    void everyFunctionalityHasCategoryAndLabel() {
        for (Functionality f : Functionality.values()) {
            assertNotNull(f.getCategory(), f.name());
            assertNotNull(f.getLabel(), f.name());
        }
    }

    @Test
    void groupsByCategoryCoveringAllValues() {
        Map<Functionality.Category, Collection<Functionality>> byCategory =
                Functionality.getFunctionalityByCategory();

        assertEquals(List.of(Functionality.CREATE_USER, Functionality.VIEW_USER,
                        Functionality.DELETE_USER),
                List.copyOf(byCategory.get(Functionality.Category.USER)));
        assertTrue(byCategory.get(Functionality.Category.GROUP)
                .contains(Functionality.ENABLE_DISABLE_GROUP));

        int total = byCategory.values().stream().mapToInt(Collection::size).sum();
        assertEquals(Functionality.values().length, total);
    }

    @Test
    void rolesConstantsMatchEnumNamesOneToOne() throws IllegalAccessException {
        java.util.Set<String> constants = new java.util.HashSet<>();
        for (java.lang.reflect.Field field : Functionality.Roles.class.getFields()) {
            String value = (String) field.get(null);
            assertEquals(field.getName(), value, "constant name and value must match");
            assertNotNull(Functionality.valueOf(value)); // throws if there is no such functionality
            constants.add(value);
        }
        assertEquals(Functionality.values().length, constants.size(),
                "every functionality needs a constant in Functionality.Roles");
    }
}

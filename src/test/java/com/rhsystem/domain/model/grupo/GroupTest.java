package com.rhsystem.domain.model.grupo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

/**
 * {@code equals}/{@code hashCode} are id-based (entity identity): instances
 * coming from different sessions/caches must compare equal — required by the
 * groups {@code Shuttle} and the {@code Set<Group>} form model.
 */
class GroupTest {

    private static Group withId(Long id, String name) {
        Group group = new Group();
        group.setId(id);
        group.setName(name);
        return group;
    }

    @Test
    void groupsWithSameIdAreEqualRegardlessOfState() {
        assertEquals(withId(1L, "Admins"), withId(1L, "Renamed"));
    }

    @Test
    void groupsWithDifferentIdsAreNotEqual() {
        assertNotEquals(withId(1L, "Admins"), withId(2L, "Admins"));
    }

    @Test
    void transientGroupsAreNeverEqual() {
        assertNotEquals(withId(null, "Admins"), withId(null, "Admins"));
    }

    @Test
    void notEqualToNullOrOtherType() {
        Group group = withId(1L, "Admins");
        assertNotEquals(group, null);
        assertNotEquals(group, "Admins");
    }

    @Test
    void hashCodeIsStableAcrossStateChanges() {
        Group group = withId(1L, "Admins");
        int before = group.hashCode();
        group.setName("Renamed");
        group.setActive(false);
        assertEquals(before, group.hashCode());
    }

    @Test
    void equalGroupsShareHashCode() {
        assertEquals(withId(7L, "A").hashCode(), withId(7L, "B").hashCode());
    }
}

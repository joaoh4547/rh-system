package com.rhsystem.domain.model.shared;

/**
 * Represents an entity that can have a deletion state.
 * This interface provides methods to check if an entity is marked as deleted
 * and to set its deletion state.
 */
public interface HasDeletion {

    /**
     * Checks whether the entity is currently marked as deleted.
     *
     * @return true if the entity is marked as deleted, false otherwise
     */
    boolean isDeleted();

    /**
     * Updates the deletion state of the entity.
     *
     * @param deleted true to mark the entity as deleted, false to mark it as not deleted
     */
    void setDeleted(boolean deleted);
}

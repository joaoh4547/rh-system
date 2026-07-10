package com.rhsystem.domain.model.shared;

/**
 * Represents an entity that can be enabled or disabled.
 */
public interface HasEnable {

    /**
     * Sets the enabled state of the entity.
     *
     * @param enable true to enable the entity, false to disable it
     */
    void setEnable(boolean enable);

    /**
     * Checks whether the entity is currently enabled.
     *
     * @return true if the entity is enabled, false otherwise
     */
    boolean isEnable();

}

package com.rhsystem.domain.model.usuario;

/**
 * Registration status of a user.
 */
public enum UserStatus {

    ACTIVE("status.ACTIVE"),
    INACTIVE("status.INACTIVE"),
    BLOCKED("status.BLOCKED"),
    PENDING_CONFIRMATION("status.PENDING_CONFIRMATION");

    private final String label;

    UserStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

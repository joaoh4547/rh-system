package com.rhsystem.application.dto.login;

/**
 * Result of credential validation performed BEFORE completing the login
 * (used to block access while terms have not been accepted).
 */
public enum LoginResult {
    OK,
    INVALID_CREDENTIALS,
    TERMS_PENDING
}

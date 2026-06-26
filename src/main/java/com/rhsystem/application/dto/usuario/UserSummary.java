package com.rhsystem.application.dto.usuario;

/**
 * Statistical summary of users — returned by the
 * {@link com.rhsystem.application.usecase.usuario.GetUserSummary} use case.
 *
 * <p>Calculated directly in the database via status counts, avoiding loading
 * the full list into memory just to display KPIs.
 */
public record UserSummary(
        long total,
        long active,
        long pending,
        long blocked
) {}

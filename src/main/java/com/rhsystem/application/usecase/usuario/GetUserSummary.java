package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.usuario.UserSummary;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: fetches the statistical user summary calculated in the database.
 *
 * <p>Does not load records into memory — executes only status counts,
 * making it suitable for displaying KPIs on paginated dashboards.
 */
@Service
public class GetUserSummary {

    private final UserRepository userRepository;

    public GetUserSummary(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserSummary execute() {
        return new UserSummary(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.ACTIVE),
                userRepository.countByStatus(UserStatus.PENDING_CONFIRMATION),
                userRepository.countByStatus(UserStatus.BLOCKED)
        );
    }
}

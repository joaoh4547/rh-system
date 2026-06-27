package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.grupo.Group;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaGroupRepository extends JpaRepository<Group, Long> {

    long countByActiveTrue();
}

package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.grupo.Group;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaGroupRepository extends JpaRepository<Group, Long> {

    long countByEnableTrue();

    java.util.List<Group> findByEnableTrueOrderByName();

    /**
     * Loads the full aggregate: the {@code functionalities} collection stays LAZY for
     * list queries (grid/cache), but is fetched in the same query here.
     */
    @EntityGraph(attributePaths = "functionalities")
    Optional<Group> findWithFunctionalitiesById(Long id);
}

package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.parameters.AppParameter;
import com.rhsystem.domain.model.parameters.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaParameterRepository extends JpaRepository<Parameter, AppParameter> {

    Optional<Parameter> findByParameter(AppParameter parameter);
}

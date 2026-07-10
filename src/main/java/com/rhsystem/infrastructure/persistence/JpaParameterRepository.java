package com.rhsystem.infrastructure.persistence;

import com.rhsystem.domain.model.parameters.AppParameter;
import com.rhsystem.domain.model.parameters.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaParameterRepository extends JpaRepository<Parameter, AppParameter> {
}

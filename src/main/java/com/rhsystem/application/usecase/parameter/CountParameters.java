package com.rhsystem.application.usecase.parameter;

import com.rhsystem.domain.repository.ParameterRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CountParameters {

    private final ParameterRepository repository;

    public long execute(){
        return repository.count();
    }
}

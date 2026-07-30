package com.rhsystem.application.dto.parameter;

import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.repository.ParameterRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class UpdateParameter {

    private ParameterRepository parameterRepository;

    public Parameter execute(UpdateParameterCommand cmd) {
        Parameter parameter = parameterRepository.findByParameter(cmd.parameter()).orElseThrow(() -> new BusinessException("error.parameter.not.found"));
        parameter.setValue(cmd.value());
        return parameterRepository.save(parameter);
    }

}

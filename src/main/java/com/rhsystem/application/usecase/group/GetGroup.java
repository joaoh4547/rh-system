package com.rhsystem.application.usecase.group;

import com.rhsystem.domain.model.Functionality.Roles;
import org.springframework.security.access.prepost.PreAuthorize;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@PreAuthorize("hasRole('" + Roles.VIEW_GROUP + "')")
@AllArgsConstructor
@Service
public class GetGroup {

    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public Group execute(Long id) {
        return groupRepository.findByIdWithFunctionalities(id)
                .orElseThrow(() -> new BusinessException("error.group.not.found"));
    }
}

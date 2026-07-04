package com.rhsystem.application.usecase.group;

import com.rhsystem.application.dto.group.UpdateGroupCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class UpdateGroup {

    private final GroupRepository groupRepository;

    @Transactional
    public Group execute(UpdateGroupCommand command) {
        Group group = groupRepository.findByIdWithFunctionalities(command.id()).orElseThrow(() -> new BusinessException("error.group.not.found"));
        group.setName(command.name());
        group.setDescription(command.description());
        group.setActive(command.active());
        group.setAdmin(command.admin());

        group.getFunctionalities().clear();
        if (command.functionalities() != null) {
            group.getFunctionalities().addAll(command.functionalities());
        }

        return groupRepository.save(group);
    }
}

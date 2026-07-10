package com.rhsystem.application.usecase.group;

import com.rhsystem.application.dto.group.CreateGroupCommand;
import com.rhsystem.application.validation.CommandValidator;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CreateGroup {

    private final CommandValidator validator;
    private final GroupRepository repository;

    @Transactional
    public Group execute(CreateGroupCommand cmd) {
        var validation = validator.check(cmd);
        validation.throwIfInvalid();
        return repository.save(transform(cmd));
    }

    private Group transform(CreateGroupCommand cmd){
    return Group.builder()
            .name(cmd.name())
            .description(cmd.description())
            .enable(cmd.active())
            .admin(cmd.admin())
            .functionalities(cmd.functionalities())
            .build();
    }
}

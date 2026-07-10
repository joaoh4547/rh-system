package com.rhsystem.application.usecase.group;

import com.rhsystem.application.dto.group.EnableGroupCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class EnableGroup {

    private GroupRepository groupRepository;

    @Transactional
    public void execute(EnableGroupCommand cmd){
        Group group = groupRepository.findById(cmd.groupId()).orElseThrow(() -> new BusinessException("error.group.not.found"));
        group.setEnable(cmd.enable());
        groupRepository.save(group);
    }
}

package com.rhsystem.application.usecase.group;

import com.rhsystem.application.dto.group.GroupSummary;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class GetGroupSummary {

    private final GroupRepository groupRepository;

    @Transactional(readOnly = true)
    public GroupSummary execute() {
        return new GroupSummary(groupRepository.count(), groupRepository.countActive());
    }

}

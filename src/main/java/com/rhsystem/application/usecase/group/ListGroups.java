package com.rhsystem.application.usecase.group;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.repository.GroupRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@AllArgsConstructor
@Service
public class ListGroups {

    private GroupRepository groupRepository;

    /** Returns all groups (for internal use / selection widgets). */
    @Transactional(readOnly = true)
    public List<Group> execute() {
        return groupRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Stream<Group> execute(int offset, int limit, Collection<Sorting> sorting){
        return groupRepository.findAllPaginated(limit,offset,sorting).stream();
    }
}

package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.interfaces.ui.shared.ActionsGrid;
import com.rhsystem.interfaces.ui.shared.ObjectActions;

import java.util.function.Function;

public class GroupGrid extends ActionsGrid<Group> {

    public GroupGrid(ObjectActions<Group> actions) {
        addColumn(Group::getName).setHeader(getTranslation("col.name")).setAutoWidth(true).setSortable(true);
        addActions(actions);
    }

    @Override
    protected Function<Group, String> createNameProvider() {
        return Group::getName;
    }
}

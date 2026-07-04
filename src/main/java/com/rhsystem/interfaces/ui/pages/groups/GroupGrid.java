package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.interfaces.ui.shared.ActionsGrid;
import com.rhsystem.interfaces.ui.shared.ObjectAction;

import java.util.Collection;
import java.util.function.Function;

public class GroupGrid extends ActionsGrid<Group> {


    protected GroupGrid(Collection<ObjectAction<Group>> objectActions) {
        super(objectActions);
    }

    @Override
    protected void configColumns() {
        addColumn("name").setHeader(getTranslation("col.name"));
        addColumn("description").setHeader(getTranslation("col.description"));
        booleanColumn("active", Group::isActive).setHeader(getTranslation("col.active")).setWidth("50px");

        setPartNameGenerator(g -> !g.isActive()? "inactive-row" : "");
    }

    @Override
    protected Function<Group, String> createNameProvider() {
        return Group::getName;
    }
}

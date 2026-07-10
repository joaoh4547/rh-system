package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.interfaces.ui.shared.ActionsGrid;
import com.rhsystem.interfaces.ui.shared.ObjectAction;

import java.util.Collection;

public class ParameterGrid extends ActionsGrid<Parameter> {

    protected ParameterGrid(Collection<ObjectAction<Parameter>> objectActions) {
        super(objectActions);
    }

    @Override
    protected void configColumns() {
        addColumn("name").setHeader(getTranslation("col.name"));
    }
}

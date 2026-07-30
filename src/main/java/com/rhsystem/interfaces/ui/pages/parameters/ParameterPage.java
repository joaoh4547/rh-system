package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.application.dto.parameter.UpdateParameter;
import com.rhsystem.application.usecase.parameter.CountParameters;
import com.rhsystem.application.usecase.parameter.ListParameters;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
import com.rhsystem.domain.model.security.ValueDecoder;
import com.rhsystem.domain.model.security.ValueEncoder;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.BasePage;
import com.rhsystem.interfaces.ui.shared.ObjectAction;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Stream;

@Route(value = "parameters", layout = MainLayout.class)
@PageTitle("Parâmetros")
@PermitAll
@AllArgsConstructor
public class ParameterPage extends BasePage<Parameter> {


    private final ListParameters listParameters;
    private final CountParameters countParameters;
    private final ValueEncoder encoder;
    private final ParameterValueConverter valueConverter;
    private final ValueDecoder decoder;
    private final UpdateParameter updateParameter;

    @Override
    protected String pageTitle() {
        return getTranslation("page.parameters.title");
    }

    @Override
    protected String pageSubtitle() {
        return getTranslation("page.parameters.subtitle");
    }

    @Override
    protected String tableTitle() {
        return getTranslation("page.parameters.table");
    }

    @Override
    protected AppGrid<Parameter> buildGrid(Collection<ObjectAction<Parameter>> objectActions) {
        return new ParameterGrid(objectActions);
    }

    @Override
    protected Dialog buildForm(@Nullable Parameter item) {
        return new ParameterFormDialog(item, this::refresh, encoder, valueConverter, decoder, updateParameter);
    }

    @Override
    protected boolean insertVisible() {
        return false;
    }

    @Override
    protected void remove(Parameter item) {

    }

    @Override
    protected Stream<Parameter> fetchResults(int limit, int offset, Collection<Sorting> sorting) {
        return listParameters.execute(limit, offset, sorting);
    }

    @Override
    protected int countResults() {
        return Math.toIntExact(countParameters.execute());
    }
}

package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.application.usecase.group.GetGroupSummary;
import com.rhsystem.application.usecase.group.ListGroups;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.StatCard;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.BasePage;
import com.rhsystem.interfaces.ui.shared.ObjectAction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * The GroupPage class represents the user interface for managing groups in the system.
 * It extends the BasePage class to provide CRUD functionality, a grid layout for group management,
 * and statistics visualization. This class adds specific behaviors and translations for groups.
 * <p>
 * Key features include:
 * - A configurable data grid for displaying groups.
 * - KPI cards to display key metrics such as total groups and active groups.
 * - Support for dynamic translations for page titles, subtitles, and labels.
 * - Integration with the GetGroupSummary use case to display group statistics.
 * <p>
 * This class is secured with `@PermitAll`, meaning it is accessible to all users.
 * The route for navigating to this page is "groups".
 */
@Route(value = "groups", layout = MainLayout.class)
@PageTitle("Groups - RH System")
@PermitAll
@AllArgsConstructor
public class GroupPage extends BasePage<Group> {


    private final GetGroupSummary getGroupSummary;
    private final ListGroups listGroups;

    @Override
    protected String pageTitle() {
        return getTranslation("page.groups.title");
    }

    @Override
    protected String pageSubtitle() {
        return getTranslation("page.groups.subtitle");
    }


    @Override
    protected AppGrid<Group> buildGrid(Collection<ObjectAction<Group>> objectActions) {
        return new GroupGrid(objectActions);
    }

    @Override
    protected Dialog buildForm(@Nullable Group item) {
        return new GroupFormDialog(item);
    }

    @Override
    protected String tableTitle() {
        return getTranslation("page.groups.table");
    }

    @Override
    protected String newButtonLabel() {
        return getTranslation("page.groups.new");
    }

    @Override
    protected Stream<Group> fetchResults(int limit, int offset, Collection<Sorting> sorting) {
        return listGroups.execute(offset, limit, sorting);
    }

    @Override
    protected int countResults() {
        return Math.toIntExact(getGroupSummary.execute().total());
    }

    @Override
    protected @Nullable Component buildStats() {
        var summary = getGroupSummary.execute();

        var container = new Div(
                new StatCard(getTranslation("page.groups.kpi.total"), summary.total(), VaadinIcon.GROUP, StatCard.Accent.PRIMARY),
                new StatCard(getTranslation("page.groups.kpi.active"), summary.active(), VaadinIcon.CHECK_CIRCLE, StatCard.Accent.SUCCESS)
        );
        container.addClassName("stats-grid");
        return container;
    }

    @Override
    protected void remove(Group item) {

    }
}

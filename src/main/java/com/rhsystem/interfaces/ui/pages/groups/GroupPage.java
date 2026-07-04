package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.application.usecase.group.CreateGroup;
import com.rhsystem.application.usecase.group.GetGroup;
import com.rhsystem.application.usecase.group.GetGroupSummary;
import com.rhsystem.application.usecase.group.ListGroups;
import com.rhsystem.application.usecase.group.UpdateGroup;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.component.StatCard;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.BasePage;
import com.rhsystem.interfaces.ui.shared.EnableDialog;
import com.rhsystem.interfaces.ui.shared.ObjectAction;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
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
    private final CreateGroup createGroup;
    private final UpdateGroup updateGroup;
    private final GetGroup getGroup;

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
        // Grid rows come detached (Hazelcast cache / closed session), so the LAZY
        // functionalities collection is not initialized. Reload the full aggregate.
        Group fullItem = item != null ? getGroup.execute(item.getId()) : null;
        return new GroupFormDialog(fullItem, createGroup, updateGroup, this::refresh);
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

        var container = new Div(new StatCard(getTranslation("page.groups.kpi.total"), summary.total(), VaadinIcon.GROUP, StatCard.Accent.PRIMARY), new StatCard(getTranslation("page.groups.kpi.active"), summary.active(), VaadinIcon.CHECK_CIRCLE, StatCard.Accent.SUCCESS));
        container.addClassName("stats-grid");
        return container;
    }

    @Override
    protected void remove(Group item) {

    }


    @Override
    public boolean canEdit(Group obj) {
        return obj.isActive();
    }

    @Override
    public boolean canDelete(Group obj) {
        return true;
    }

    @Override
    protected Collection<ObjectAction<Group>> createAdditionalActions() {
        Collection<ObjectAction<Group>> actions = new ArrayList<>();
        actions.add(createDisableAction());
        actions.add(createEnableAction());
        return actions;
    }

    private ObjectAction<Group> createDisableAction() {
        return ObjectAction.<Group>builder()
                .label(getTranslation("action.disable"))
//                .enabled()
                .icon(LucideIcon::lock)
                .handler(x ->{
                    createEnable(false, x).open();
                })
//                .visible(Group::isActive)
                .build();
    }

    public ObjectAction<Group> createEnableAction() {
        return ObjectAction.<Group>builder()
                .label(getTranslation("action.enable"))
                .icon(LucideIcon::unLock)
                .handler(x -> {
                    createEnable(true, x).open();
                })
                .visible(g -> !g.isActive())
                .build();
    }

    private void enable(Group group) {
    }

    private void disable(Group o) {
    }

    @Override
    protected String getEntityArticle() {
        return getTranslation("masc.article");
    }

    private EnableDialog<Group> createEnable(boolean enable, Group obj) {
        return new EnableDialog<>(obj, (g) -> {
            if (enable) {
                enable(g);
            } else {
                disable(g);
            }
        }, Group::getName, "Grupo", getEntityArticle(), enable);
    }

    public void confirmEnable(Group obj) {

    }


}

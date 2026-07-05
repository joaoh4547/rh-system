package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.application.usecase.group.ListGroups;
import com.rhsystem.application.usecase.usuario.*;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.usuario.User;
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
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * User management page.
 *
 * <p>The grid loads users with database-level pagination ({@link ListUsers}).
 * KPIs are calculated by {@link GetUserSummary} via aggregate counts — independent
 * of the grid's current page.
 */
@Route(value = "usuarios", layout = MainLayout.class)
@PageTitle("Users - RH System")
@PermitAll
public class UserPage extends BasePage<User> {

    private final ListUsers listUsers;
    private final CreateUser createUser;
    private final UpdateUser updateUser;
    private final RemoveUser removeUser;
    private final GetUserSummary getUserSummary;
    private final GetUser getUser;
    private final ListGroups listGroups;

    public UserPage(ListUsers listUsers,
                    CreateUser createUser,
                    UpdateUser updateUser,
                    RemoveUser removeUser,
                    GetUserSummary getUserSummary,
                    GetUser getUser,
                    ListGroups listGroups) {
        this.listUsers = listUsers;
        this.createUser = createUser;
        this.updateUser = updateUser;
        this.removeUser = removeUser;
        this.getUserSummary = getUserSummary;
        this.getUser = getUser;
        this.listGroups = listGroups;
    }

    @Override
    protected String pageTitle() {
        return getTranslation("page.users.title");
    }

    @Override
    protected String pageSubtitle() {
        return getTranslation("page.users.subtitle");
    }

    @Override
    protected String tableTitle() {
        return getTranslation("page.users.table");
    }

    @Override
    protected String newButtonLabel() {
        return getTranslation("page.users.new");
    }


    @Override
    protected Stream<User> fetchResults(int limit, int offset, Collection<Sorting> sorting) {
        return listUsers.execute(offset, limit, sorting);
    }

    @Override
    protected int countResults() {
        return Math.toIntExact(getUserSummary.execute().total());
    }

    @Override
    protected Component buildStats() {
        var summary = getUserSummary.execute();
        var container = new Div(
                new StatCard(getTranslation("page.users.kpi.total"), summary.total(), VaadinIcon.USERS, StatCard.Accent.PRIMARY),
                new StatCard(getTranslation("page.users.kpi.active"), summary.active(), VaadinIcon.CHECK_CIRCLE, StatCard.Accent.SUCCESS),
                new StatCard(getTranslation("page.users.kpi.pending"), summary.pending(), VaadinIcon.HOURGLASS, StatCard.Accent.WARNING),
                new StatCard(getTranslation("page.users.kpi.blocked"), summary.blocked(), VaadinIcon.BAN, StatCard.Accent.DANGER)
        );
        container.addClassName("stats-grid");
        return container;
    }


    @Override
    protected AppGrid<User> buildGrid(Collection<ObjectAction<User>> objectActions) {
        return new UserGrid(objectActions);
    }

    @Override
    protected Dialog buildForm(@Nullable User user) {
        // Reload with the groups collection fetched: the grid row is a detached
        // entity, reading its lazy groups here would throw LazyInitializationException.
        User editing = user == null ? null : getUser.execute(user.getId());
        return new UserFormDialog(createUser, updateUser, editing, listGroups.executeActive(), this::refresh);
    }

    @Override
    protected void remove(User user) {
        removeUser.execute(user.getId());
    }

    @Override
    protected String getEntityArticle() {
        return getTranslation("masc.article");
    }
}

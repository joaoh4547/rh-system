package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.application.usecase.usuario.CreateUser;
import com.rhsystem.application.usecase.usuario.GetUserSummary;
import com.rhsystem.application.usecase.usuario.ListUsers;
import com.rhsystem.application.usecase.usuario.RemoveUser;
import com.rhsystem.application.usecase.usuario.UpdateUser;
import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.StatCard;
import com.rhsystem.interfaces.ui.shared.AppGrid;
import com.rhsystem.interfaces.ui.shared.BasePage;
import com.rhsystem.interfaces.ui.shared.ObjectActions;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.PermitAll;

import java.util.Collection;
import java.util.stream.Collectors;

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

    public UserPage(ListUsers listUsers,
                    CreateUser createUser,
                    UpdateUser updateUser,
                    RemoveUser removeUser,
                    GetUserSummary getUserSummary) {
        this.listUsers = listUsers;
        this.createUser = createUser;
        this.updateUser = updateUser;
        this.removeUser = removeUser;
        this.getUserSummary = getUserSummary;
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
    protected DataProvider<User, Void> buildDataProvider() {

        return DataProvider.fromCallbacks(
                query -> {
                    Collection<Sorting> sorting = query.getSortOrders().stream()
                            .map(sortOrder -> new Sorting(sortOrder.getSorted(), sortOrder.getDirection() == SortDirection.ASCENDING ? Sorting.Direction.ASC : Sorting.Direction.DESC))
                            .toList();
                    return listUsers.execute(query.getOffset(), query.getLimit(), sorting);
                },
                query -> Math.toIntExact(getUserSummary.execute().total())
        );
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
    protected AppGrid<User> buildGrid(ObjectActions<User> actions) {
        return new UserGrid(actions);
    }

    @Override
    protected Dialog buildForm(@Nullable User user) {
        return new UserFormDialog(createUser, updateUser, user, this::refresh);
    }

    @Override
    protected void remove(User user) {
        removeUser.execute(user.getId());
    }
}

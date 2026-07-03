package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.domain.model.Sorting;
import com.rhsystem.domain.validation.ValidationException;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * Base page for CRUDs with persistence via services/use-cases.
 *
 * <p><b>Responsibilities of this layer:</b>
 * <ul>
 *   <li>Full page layout (header, KPIs, card with grid).</li>
 *   <li>Database-level paginated grid via {@link DataProvider#fromCallbacks}.</li>
 *   <li>KPIs calculated by a dedicated use case — do not depend on the loaded list.</li>
 *   <li>Persistence cycle: {@link #remove(Object)}.</li>
 * </ul>
 *
 * <p><b>Difference from {@link DataEditor}:</b>
 * {@code DataEditor} keeps data in memory (for sub-editors embedded in forms).
 * {@code BasePage} <em>never</em> loads all records — always delegates to the database
 * via {@link DataProvider} and uses aggregate use cases for KPIs.
 *
 * <h3>Subclass contract</h3>
 * <pre>{@code
 * @Route("users")
 * public class UserPage extends BasePage<User> {
 *
 *     @Override
 *     protected DataProvider<User, Void> buildDataProvider() {
 *         return DataProvider.fromCallbacks(
 *             q -> listUsers.execute(q.getOffset(), q.getLimit()),
 *             q -> getUserSummary.execute().total()
 *         );
 *     }
 *
 *     @Override
 *     protected Component buildStats() {
 *         var summary = getUserSummary.execute();
 *         return new Div(new StatCard("Total", summary.total(), ...));
 *     }
 *
 *     @Override
 *     protected void remove(User u) { removeUser.execute(u.getId()); }
 * }
 * }</pre>
 *
 * @param <T> type of the record managed by the page
 */
public abstract class BasePage<T> extends DataEditor<T> {

    private final Div statsContainer = new Div();
    private final Div card = new Div();

    private DataProvider<T, Void> dataProvider;

    protected BasePage() {
        addClassName("view");
        setPadding(true);
        setSpacing(true);
    }

    protected Stream<T> fetchData(Query<T, ?> query) {
        Collection<Sorting> sorting = query.getSortOrders().stream()
                .map(sortOrder -> new Sorting(sortOrder.getSorted(), sortOrder.getDirection() == SortDirection.ASCENDING ? Sorting.Direction.ASC : Sorting.Direction.DESC))
                .toList();
        return fetchResults(query.getLimit(), query.getOffset(), sorting);
    }

    protected abstract Stream<T> fetchResults(int limit, int offset, Collection<Sorting> sorting);

    protected abstract int countResults();

    /**
     * Initialised by Spring after DI of the subclass — ensures use cases are available.
     */
    @PostConstruct
    private void initialize() {
        grid = buildGrid(creatActions());
        dataProvider = buildDataProvider();
        grid.setDataProvider(dataProvider);
        buildLayout();
        refreshKpis();
    }

    /**
     * Disables DataEditor's onAttach: BasePage uses @PostConstruct.
     */
    @Override
    protected void onAttach(AttachEvent event) {
        // intentional: initialisation via @PostConstruct
    }

    @Override
    protected void buildLayout() {
        statsContainer.addClassName("stats-grid");
        card.addClassName("card");
        card.setSizeFull();
        setFlexGrow(1, card);
        card.add(buildToolbar(tableTitle(), newButtonLabel()), grid);
        add(buildHeader(), statsContainer, card);
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    protected abstract String pageTitle();

    protected abstract String pageSubtitle();

    @Override
    protected abstract String tableTitle();

    @Override
    protected abstract String newButtonLabel();

    // ── DataProvider (server-side pagination) ─────────────────────────────────

    /**
     * Creates the {@link DataProvider} that feeds the grid with database-level pagination.
     * Vaadin calls the callbacks with {@code offset/limit} on each scroll or sort.
     *
     * <pre>{@code
     * return DataProvider.fromCallbacks(
     *     q -> listUsers.execute(q.getOffset(), q.getLimit()),
     *     q -> getUserSummary.execute().total()
     * );
     * }</pre>
     */
    protected final DataProvider<T, Void> buildDataProvider() {
        return DataProvider.fromCallbacks(this::fetchData,
                q -> countResults());
    }

    // ── KPIs ──────────────────────────────────────────────────────────────────

    /**
     * Returns the KPI component rendered above the card.
     *
     * <p>Implement by calling a dedicated aggregate use case — does not depend
     * on the grid's paginated list. Return {@code null} to omit the area.
     */
    @Nullable
    protected Component buildStats() {
        return null;
    }

    // ── Persistence ───────────────────────────────────────────────────────────

    /**
     * Removes the record via service/repository.
     * Throws {@link ValidationException} to signal business errors.
     */
    protected abstract void remove(T item);

    @Override
    protected void executeRemoval(T item) {
        try {
            remove(item);
            refresh();
            notifySuccess(getTranslation("notify.removed"));
        } catch (ValidationException e) {
            ValidationNotifier.show(this::getTranslation, e);
        }
    }

    /**
     * Reloads the grid (triggers a new fetch/count in the database) and refreshes the KPIs.
     * Use {@code this::refresh} as a callback in form dialogs.
     */
    @Override
    public void refresh() {
        dataProvider.refreshAll();
        refreshKpis();
    }

    // ── Layout ────────────────────────────────────────────────────────────────

    private Div buildHeader() {
        var title = new H2(pageTitle());
        title.addClassNames(LumoUtility.Margin.NONE);
        var subtitle = new Span(pageSubtitle());
        subtitle.addClassName("page-subtitle");
        var header = new Div(title, subtitle);
        header.addClassName("page-header");
        return header;
    }

    private void refreshKpis() {
        statsContainer.removeAll();
        var stats = buildStats();
        if (stats != null) statsContainer.add(stats);
    }
}

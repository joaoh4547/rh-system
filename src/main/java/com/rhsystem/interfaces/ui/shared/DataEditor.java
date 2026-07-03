package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * In-memory CRUD component — manages a list of items of type {@code T}
 * with add, edit and delete without persistence.
 *
 * <p>Ideal for editing sub-lists within forms (e.g. documents of a user).
 * For full pages with persistence, extend {@link BasePage}.
 *
 * <h3>Subclass contract</h3>
 * <ul>
 *   <li>{@link #buildGrid(Collection)} — configure columns and return the {@link AppGrid}.</li>
 *   <li>{@link #buildForm(Object)} — create the edit/add {@link Dialog}.
 *       The dialog should call {@link #addItem}, {@link #replaceItem} or
 *       {@link #refresh} after saving.</li>
 *   <li>{@link #tableTitle()} and {@link #newButtonLabel()} — toolbar texts.</li>
 * </ul>
 *
 * @param <T> type of item managed
 */
public abstract class DataEditor<T> extends VerticalLayout {

    /**
     * Main grid — available after the first component attach.
     */
    protected AppGrid<T> grid;

    private final List<T> data = new ArrayList<>();

    protected DataEditor() {
        setSizeFull();
        setPadding(false);
        setSpacing(false);
    }

    protected DataEditor(java.util.Collection<T> initialData) {
        this();
        data.addAll(initialData);
    }

    protected DataEditor(java.util.stream.Stream<T> initialData) {
        this();
        try (initialData) {
            initialData.forEach(data::add);
        }
    }

    public DataEditor<T> withData(java.util.Collection<T> items) {
        setData(new ArrayList<>(items));
        return this;
    }

    public DataEditor<T> withData(java.util.stream.Stream<T> stream) {
        try (stream) {
            setData(stream.collect(java.util.stream.Collectors.toList()));
        }
        return this;
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    /**
     * Builds the layout the first time the component is attached to the DOM,
     * ensuring subclass fields are already injected.
     * {@link BasePage} disables this via {@code @PostConstruct}.
     */
    @Override
    protected void onAttach(AttachEvent event) {
        super.onAttach(event);
        if (grid == null) {
            grid = buildGrid(creatActions());
            buildLayout();
            syncGrid();
        }
    }


    protected final Collection<ObjectAction<T>> creatActions() {
        Collection<ObjectAction<T>> actions = new ArrayList<>();
        actions.add(createEditAction());
        actions.add(createDeleteAction());
        actions.addAll(createAdditionalActions());
        return actions;
    }

    protected ObjectAction<T> createEditAction() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.edit"))
                .icon(LucideIcon::edit)
                .handler(this::openForm)
                .build();
    }

    protected ObjectAction<T> createDeleteAction() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.remove"))
                .buttonVariant(ButtonVariant.LUMO_ERROR)
                .icon(LucideIcon::delete)
                .handler(this::confirmRemoval)
                .build();
    }

    protected final Collection<ObjectAction<T>> createAdditionalActions() {
        return new ArrayList<>();
    }

    protected void buildLayout() {
        var card = new Div();
        card.addClassName("card");
        card.setSizeFull();
        setFlexGrow(1, card);
        card.add(buildToolbar(tableTitle(), newButtonLabel()), grid);
        add(card);
    }

    // ── Abstract methods ──────────────────────────────────────────────────────

    protected abstract AppGrid<T> buildGrid(Collection<ObjectAction<T>> actions);

    protected abstract Dialog buildForm(@Nullable T item);

    protected String tableTitle() {
        return "Records";
    }

    protected String newButtonLabel() {
        return "New";
    }

    // ── Form ──────────────────────────────────────────────────────────────────

    protected void openForm(@Nullable T item) {
        buildForm(item).open();
    }

    // ── Removal ───────────────────────────────────────────────────────────────

    protected void confirmRemoval(T item) {
        var dlg = new ConfirmDialog();
        dlg.setHeader(getTranslation("confirm.remove.header"));
        dlg.setText(getTranslation("confirm.remove.text", createDisplayNameExtractor().apply(item)));
        dlg.setCancelable(true);
        dlg.setCancelText(getTranslation("action.cancel"));
        dlg.setConfirmText(getTranslation("confirm.remove.button"));
        dlg.setConfirmButtonTheme("error primary");
        dlg.addConfirmListener(e -> executeRemoval(item));
        dlg.open();
    }

    protected Function<T, String> createDisplayNameExtractor() {
        return Object::toString;
    }

    protected void executeRemoval(T item) {
        data.remove(item);
        syncGrid();
    }

    // ── In-memory helpers ─────────────────────────────────────────────────────

    protected void addItem(T item) {
        data.add(item);
        syncGrid();
    }

    protected void replaceItem(T old, T replacement) {
        int idx = data.indexOf(old);
        if (idx >= 0) {
            data.set(idx, replacement);
        } else {
            data.add(replacement);
        }
        syncGrid();
    }

    protected void refresh() {
        syncGrid();
    }

    protected void setData(List<T> items) {
        data.clear();
        data.addAll(items);
        syncGrid();
    }

    protected void setData(java.util.stream.Stream<T> stream) {
        try (stream) {
            setData(stream.collect(java.util.stream.Collectors.toList()));
        }
    }

    protected List<T> getData() {
        return Collections.unmodifiableList(data);
    }

    private void syncGrid() {
        if (grid != null) {
            grid.setItems(new ArrayList<>(data));
        }
    }

    // ── UI helpers ────────────────────────────────────────────────────────────

    protected HorizontalLayout buildToolbar(String title, String newLabel) {
        var t = new Span(title);
        t.addClassName("card-title");

        var btnNew = new Button(newLabel, LucideIcon.add(),
                e -> openForm(null));
        btnNew.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        var toolbar = new HorizontalLayout(t, btnNew);
        toolbar.setWidthFull();
        toolbar.setAlignItems(FlexComponent.Alignment.CENTER);
        toolbar.expand(t);
        toolbar.addClassName("card-toolbar");
        return toolbar;
    }

    protected void notifySuccess(String message) {
        Notification.show(message).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    protected void notifyError(String message) {
        Notification.show(message).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}

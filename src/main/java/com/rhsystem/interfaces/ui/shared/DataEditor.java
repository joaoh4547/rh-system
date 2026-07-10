package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.domain.model.shared.HasDeletion;
import com.rhsystem.domain.model.shared.HasEnable;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.utils.Reflections;
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

import java.util.*;
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
            grid = buildGrid(createActions());
            buildLayout();
            syncGrid();
        }
    }


    protected final Collection<ObjectAction<T>> createActions() {
        Collection<ObjectAction<T>> actions = new ArrayList<>(createAdditionalActions());
        if(implementsType(HasEnable.class)){
            actions.add(createEnableAction());
            actions.add(createDisableAction());
        }
        actions.add(createEditAction());
        if(implementsType(HasDeletion.class)){
            actions.add(createRestore());
        }
        actions.add(createDeleteAction());
        return actions;
    }

    protected ObjectAction<T> createEditAction() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.edit"))
                .icon(LucideIcon::edit)
                .handler(this::openForm)
                .enabled(this::canEdit)
                .visible(this::editVisible)
                .build();
    }

    public boolean canEdit(T obj) {
        return true;
    }

    public boolean canDelete(T obj) {
        return canEdit(obj);
    }


    protected ObjectAction<T> createDeleteAction() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.remove"))
                .buttonVariant(ButtonVariant.LUMO_ERROR)
                .icon(LucideIcon::delete)
                .handler(this::confirmRemoval)
                .enabled(this::canDelete)
                .visible(obj -> {
                    var visible = deleteVisible(obj);
                    if (implementsType(HasDeletion.class)) {
                        return !isDeleted(obj) && visible;
                    }
                    return visible;
                })
                .build();
    }


    protected ObjectAction<T> createRestore() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.restore"))
                .icon(LucideIcon::restore)
                .enabled(this::canRestore)
                .handler(this::confirmRestore)
                .visible(obj -> {
                    if (implementsType(HasDeletion.class)) {
                        return isDeleted(obj);
                    }
                    return false;
                })
                .build();
    }


    protected void confirmRestore(T obj) {
        var dlg = new ConfirmDialog();
        dlg.setHeader(getTranslation("confirm.restore.header"));
        dlg.setText(getTranslation("confirm.restore.text", createDisplayNameExtractor().apply(obj)));
        dlg.setCancelable(true);
        dlg.setCancelText(getTranslation("action.cancel"));
        dlg.setConfirmText(getTranslation("confirm.remove.button"));
        dlg.setConfirmButtonTheme("error primary");
        dlg.addConfirmListener(e -> executeRestore(obj));
        dlg.open();
    }


    protected boolean canRestore(T obj) {
        return canEdit(obj) && !isDeleted(obj);
    }


    protected boolean insertVisible() {
        return true;
    }

    private boolean isDeleted(T obj) {
        return implementsType(HasDeletion.class, obj).isDeleted();
    }

    protected boolean deleteVisible(T obj) {
        return true;
    }

    protected boolean editVisible(T obj) {
        return true;
    }

    protected Collection<ObjectAction<T>> createAdditionalActions() {
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
        String deleteKey = implementsType(HasDeletion.class) ? "confirm.remove-with-restore" : "confirm.remove.text";
        var dlg = new ConfirmDialog();
        dlg.setHeader(getTranslation("confirm.remove.header"));
        dlg.setText(getTranslation(deleteKey, createDisplayNameExtractor().apply(item)));
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
        if (!implementsType(HasDeletion.class)) {
            data.remove(item);
        } else {
            implementsType(HasDeletion.class, item).setDeleted(true);
        }
        syncGrid();
    }

    protected void executeRestore(T obj) {
        implementsType(HasDeletion.class, obj).setDeleted(false);
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
        btnNew.setVisible(insertVisible());

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

    @SuppressWarnings("unchecked")
    protected Class<T> getType() {
        return (Class<T>) Reflections.getGenericType(getClass(), 0);
    }

    protected Function<T, String> entityTextProvider() {
        return Object::toString;
    }

    protected boolean implementsType(Class<?> type) {
        return type.isAssignableFrom(getType());
    }

    protected <I> I implementsType(Class<I> type, Object target) {
        if (type.isAssignableFrom(getType())) {
            return type.cast(target);
        }
        return null;
    }

    protected String getEntityName() {
        return null;
    }

    protected void enable(T obj) {
        throw new UnsupportedOperationException("Not implemented");
    }

    protected void disable(T obj) {
        throw new UnsupportedOperationException("Not implemented");
    }


    private EnableDialog<T> createEnable(boolean enable, T target) {
        return new EnableDialog<T>(target, (o) -> {
            if (enable) {
                enable(o);
            } else {
                disable(o);
            }
            refresh();
        }, entityTextProvider(), getEntityName(), getEntityArticle(), enable);
    }

    protected ObjectAction<T> createDisableAction() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.disable"))
                .icon(LucideIcon::lock)
                .handler(x -> createEnable(false, x).open())
                .visible(this::isEnable)
                .build();
    }

    private boolean isEnable(T obj) {
        return Objects.requireNonNull(implementsType(HasEnable.class, obj)).isEnable();
    }

    protected ObjectAction<T> createEnableAction() {
        return ObjectAction.<T>builder()
                .label(getTranslation("action.enable"))
                .icon(LucideIcon::unLock)
                .handler(x -> createEnable(true, x).open())
                .visible(g -> !isEnable(g))
                .build();
    }


    protected String getEntityArticle() {
        return "";
    }
}

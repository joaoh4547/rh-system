package com.rhsystem.interfaces.ui.shared;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Set of CRUD actions available for an object type {@code T}.
 *
 * <p>Centralises operation callbacks (edit, remove, etc.) in a single object,
 * so {@link AppGrid} and {@link BasePage} don't need to know each callback individually.
 *
 * <p>Created by {@link DataEditor} from its own methods and passed to
 * {@code buildGrid(ObjectActions)} — the {@link AppGrid} uses the actions
 * without knowing who implements them.
 *
 * @param <T> type of the object to which the actions apply
 */
public final class ObjectActions<T> {

    private final Consumer<T> onEdit;
    private final BiConsumer<T, String> onRemove;

    private ObjectActions(Builder<T> builder) {
        this.onEdit   = builder.onEdit;
        this.onRemove = builder.onRemove;
    }

    /** Triggers the edit action. No-op if not configured. */
    public void edit(T item) {
        if (onEdit != null) onEdit.accept(item);
    }

    /**
     * Triggers the remove action, passing the item name for display
     * in the confirmation dialog. No-op if not configured.
     *
     * @param item        item to remove
     * @param displayName text shown in the confirmation message
     */
    public void remove(T item, String displayName) {
        if (onRemove != null) onRemove.accept(item, displayName);
    }

    public boolean canEdit()   { return onEdit   != null; }
    public boolean canRemove() { return onRemove != null; }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static final class Builder<T> {

        private Consumer<T> onEdit;
        private BiConsumer<T, String> onRemove;

        private Builder() {}

        public Builder<T> edit(Consumer<T> action) {
            this.onEdit = action;
            return this;
        }

        public Builder<T> remove(BiConsumer<T, String> action) {
            this.onRemove = action;
            return this;
        }

        public ObjectActions<T> build() {
            return new ObjectActions<>(this);
        }
    }
}

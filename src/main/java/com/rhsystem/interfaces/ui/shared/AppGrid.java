package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.domain.model.shared.HasDeletion;
import com.rhsystem.domain.model.shared.HasEnable;
import com.rhsystem.utils.Reflections;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.function.ValueProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Base grid for the system with default styling applied automatically.
 *
 * <p>All grids in the system should extend this class to ensure visual
 * consistency without repeating configuration.
 *
 * @param <T> type of item displayed in the grid
 */
public abstract class AppGrid<T> extends Grid<T> {

    public AppGrid(Class<T> beanType) {
        super(beanType, false);
        configure();
    }

    public AppGrid() {
        super();
        getGridType().ifPresent(type -> {
            configureBeanType(type, false);
        });

        configure();
    }

    protected abstract void configColumns();

    /**
     * Configures the grid with default settings, columns, and styles.
     * <p>
     * This method applies the following configurations:
     * - Adds theme variants for row stripes and no border styling.
     * - Sets the grid to occupy the full available size.
     * - Invokes {@link #configColumns()} to configure specific columns for the grid.
     * <p>
     * Additionally, it checks for the presence of specific interfaces on the grid's
     * type and dynamically adds corresponding boolean columns:
     * - If the type implements {@link HasEnable}, a column labeled "Active" is added
     * to display and handle the "enabled" status.
     * - If the type implements {@link HasDeletion}, a column labeled "Deleted" is added
     * to display and handle the deletion status.
     */
    private void configure() {
        addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER
        );
        setSizeFull();
        configColumns();

        if (implementsType(HasEnable.class)) {
            booleanColumn("enabled", this::isActive)
                    .setHeader(getTranslation("col.active"))
                    .setWidth("50px");
        }
        if (implementsType(HasDeletion.class)) {
            booleanColumn("delete", this::isDeleted)
                    .setHeader(getTranslation("col.deleted"))
                    .setWidth("50px");
        }

        setPartNameGenerator(obj ->{
            var classes = new ArrayList<String>();
            if (!isActive(obj)){
                classes.add("inactive-row");
            }

            if(isDeleted(obj)){
                classes.add("deleted-row");
            }
            return String.join(" ", classes);
        });
    }

    private boolean isActive(T obj){
        if(obj instanceof HasEnable e){
            return e.isEnable();
        }
        return true;
    }

    private boolean isDeleted(T obj){
        if(obj instanceof HasDeletion d){
            return d.isDeleted();
        }
        return false;
    }

    private boolean implementsType(Class<?> type) {
        return type.isAssignableFrom(getBeanType());
    }


    public Optional<Class<T>> getGridType() {
        var type = Reflections.getGenericType(getClass(), 0);
        if (type != null) {
            return Optional.of((Class<T>) type);
        }
        return Optional.empty();
    }

    protected Column<T> addColumn(String property, ValueProvider<T, ?> provider) {
        return addColumn(provider).setSortProperty(property);
    }

    public Column<T> booleanColumn(String property, ValueProvider<T, Boolean> provider) {
        return addColumn(property, x -> provider.apply(x) ? "Sim" : "Não");
    }
}

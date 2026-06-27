package com.rhsystem.interfaces.ui.shared;

import com.rhsystem.utils.Reflections;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

import java.util.Optional;

/**
 * Base grid for the system with default styling applied automatically.
 *
 * <p>All grids in the system should extend this class to ensure visual
 * consistency without repeating configuration.
 *
 * @param <T> type of item displayed in the grid
 */
public class AppGrid<T> extends Grid<T> {

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

    private void configure() {
        addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER
        );
        setSizeFull();
    }


    public Optional<Class<T>> getGridType() {
        var type = Reflections.getGenericType(getClass(), 0);
        if (type != null) {
            return Optional.of((Class<T>) type);
        }
        return Optional.empty();
    }
}

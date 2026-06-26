package com.rhsystem.interfaces.ui.shared;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

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
        configure();
    }

    private void configure() {
        addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER
        );
        setSizeFull();
    }
}

package com.rhsystem.interfaces.ui.shared;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/**
 * Grid base do sistema com estilização padrão aplicada automaticamente.
 *
 * <p>Todas as grids do sistema devem herdar desta classe para garantir
 * consistência visual sem repetição de configuração.
 *
 * <pre>{@code
 * AppGrid<Usuario> grid = new AppGrid<>(Usuario.class);
 * grid.addColumn(Usuario::getNome).setHeader("Nome").setAutoWidth(true);
 * }</pre>
 *
 * @param <T> tipo do item exibido na grid
 */
public class AppGrid<T> extends Grid<T> {

    public AppGrid(Class<T> beanType) {
        super(beanType, false);
        configurar();
    }

    public AppGrid() {
        super();
        configurar();
    }

    private void configurar() {
        addThemeVariants(
                GridVariant.LUMO_ROW_STRIPES,
                GridVariant.LUMO_NO_BORDER
        );
        setSizeFull();
    }
}

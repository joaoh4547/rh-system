package com.rhsystem.interfaces.ui.pages.cache;

import com.rhsystem.application.usecase.cache.ClearCache;
import com.rhsystem.application.usecase.cache.GetCacheStats;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.stream.Collectors;

@Route(value = "cache", layout = MainLayout.class)
@PageTitle("Gerenciamento de Cache")
@PermitAll
public class CachePage extends VerticalLayout {

    private final GetCacheStats getCacheStats;
    private final ClearCache clearCache;
    private final Grid<CacheRecord> grid = new Grid<>(CacheRecord.class);

    public CachePage(GetCacheStats getCacheStats, ClearCache clearCache) {
        this.getCacheStats = getCacheStats;
        this.clearCache = clearCache;

        setSizeFull();
        add(new H2("Gerenciamento de Cache"));

        Button clearAllButton = new Button("Limpar Tudo", LucideIcon.delete().create(), e -> {
            clearCache.execute(null);
            Notification.show("Todos os caches foram limpos.");
            refreshGrid();
        });
        clearAllButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(clearAllButton);

        grid.addColumn(CacheRecord::name).setHeader("Nome");
        grid.addColumn(CacheRecord::size).setHeader("Tamanho (Entradas)");
        grid.addComponentColumn(cache -> {
            Button clearButton = new Button(LucideIcon.delete().create(), e -> {
                clearCache.execute(cache.name());
                Notification.show("Cache '" + cache.name() + "' limpo.");
                refreshGrid();
            });
            clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            return clearButton;
        }).setHeader("Ações");

        add(grid);
        refreshGrid();
    }

    private void refreshGrid() {
        grid.setItems(getCacheStats.execute().entrySet().stream()
                .map(e -> new CacheRecord(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    public record CacheRecord(String name, long size) {}
}

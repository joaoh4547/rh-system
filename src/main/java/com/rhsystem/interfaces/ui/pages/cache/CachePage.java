package com.rhsystem.interfaces.ui.pages.cache;

import com.rhsystem.application.port.CacheDetail;
import com.rhsystem.application.port.CacheEntry;
import com.rhsystem.application.usecase.cache.ClearCache;
import com.rhsystem.application.usecase.cache.GetCacheStats;
import com.rhsystem.interfaces.ui.MainLayout;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.component.StatCard;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@Route(value = "cache", layout = MainLayout.class)
@PageTitle("Gerenciamento de Cache")
@PermitAll
public class CachePage extends VerticalLayout {

    private final GetCacheStats getCacheStats;
    private final ClearCache clearCache;
    private final Grid<CacheDetail> grid = new Grid<>(CacheDetail.class, false);
    private final Div statsArea = new Div();

    public CachePage(GetCacheStats getCacheStats, ClearCache clearCache) {
        this.getCacheStats = getCacheStats;
        this.clearCache = clearCache;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        add(buildHeader());

        statsArea.addClassName("stats-grid");
        add(statsArea);

        configureGrid();
        add(grid);

        refresh();
    }

    private Div buildHeader() {
        H2 title = new H2("Gerenciamento de Cache");
        title.getStyle().set("margin", "0");

        Paragraph subtitle = new Paragraph(
                "Consulte o consumo de memória, limpe caches e expanda uma linha para ver cada entrada armazenada.");
        subtitle.getStyle().set("margin", "0").set("color", "var(--lumo-secondary-text-color)");

        Button clearAllButton = new Button("Limpar todos os caches", LucideIcon.delete(), e -> {
            this.clearCache.execute(null);
            notifySuccess("Todos os caches foram limpos.");
            refresh();
        });
        clearAllButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout topRow = new HorizontalLayout(new Div(title, subtitle), clearAllButton);
        topRow.setWidthFull();
        topRow.setAlignItems(FlexComponent.Alignment.CENTER);
        topRow.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        Div wrapper = new Div(topRow);
        wrapper.setWidthFull();
        return wrapper;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        grid.addColumn(CacheDetail::name).setHeader("Cache").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(CacheDetail::entryCount).setHeader("Entradas").setAutoWidth(true);
        grid.addColumn(c -> humanReadable(c.memoryBytes())).setHeader("Memória").setAutoWidth(true);
        grid.addColumn(CacheDetail::hits).setHeader("Acessos com sucesso").setAutoWidth(true);
        grid.addComponentColumn(this::buildRowActions).setHeader("Ações").setAutoWidth(true)
                .setTextAlign(com.vaadin.flow.component.grid.ColumnTextAlign.END);

        grid.setItemDetailsRenderer(new ComponentRenderer<>(this::buildDetails));
        grid.setDetailsVisibleOnClick(true);
    }

    private Button buildRowActions(CacheDetail cache) {
        Button clearButton = new Button(LucideIcon.delete(), e -> {
            this.clearCache.execute(cache.name());
            notifySuccess("Cache '" + cache.name() + "' limpo.");
            refresh();
        });
        clearButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_ICON);
        clearButton.getElement().setAttribute("title", "Limpar este cache");
        return clearButton;
    }

    private VerticalLayout buildDetails(CacheDetail cache) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(true);
        layout.setSpacing(true);
        layout.getStyle().set("background", "var(--lumo-contrast-5pct)");

        Div metrics = new Div();
        metrics.addClassName("stats-grid");
        metrics.add(new StatCard("Entradas nesta instância", cache.entryCount(), VaadinIcon.DATABASE, StatCard.Accent.PRIMARY));
        metrics.add(new StatCard("Cópias de outras instâncias", cache.backupEntryCount(), VaadinIcon.COPY, StatCard.Accent.WARNING));
        metrics.add(new StatCard("Memória (nesta instância)", humanReadable(cache.memoryBytes()), VaadinIcon.CHART, StatCard.Accent.SUCCESS));
        metrics.add(new StatCard("Memória (cópias)", humanReadable(cache.backupMemoryBytes()), VaadinIcon.CHART, StatCard.Accent.WARNING));
        metrics.add(new StatCard("Acessos com sucesso", cache.hits(), VaadinIcon.BULLSEYE, StatCard.Accent.PRIMARY));
        metrics.add(new StatCard("Consultas realizadas", cache.getCount(), VaadinIcon.DOWNLOAD, StatCard.Accent.PRIMARY));
        layout.add(metrics);

        H4 entriesTitle = new H4("Entradas do cache");
        entriesTitle.getStyle().set("margin", "0");
        layout.add(entriesTitle);
        layout.add(buildEntriesGrid(cache.name()));
        return layout;
    }

    private com.vaadin.flow.component.Component buildEntriesGrid(String cacheName) {
        List<CacheEntry> entries = getCacheStats.executeEntries(cacheName);
        if (entries.isEmpty()) {
            Span empty = new Span("Este cache não possui entradas armazenadas no momento.");
            empty.getStyle().set("color", "var(--lumo-secondary-text-color)");
            return empty;
        }
        Grid<CacheEntry> entriesGrid = new Grid<>(CacheEntry.class, false);
        entriesGrid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_COMPACT);
        entriesGrid.addColumn(CacheEntry::key).setHeader("Chave").setAutoWidth(true).setFlexGrow(1);
        entriesGrid.addColumn(CacheEntry::valueType).setHeader("Tipo").setAutoWidth(true);
        entriesGrid.addColumn(CacheEntry::valuePreview).setHeader("Valor").setAutoWidth(true).setFlexGrow(1);
        entriesGrid.addColumn(e -> humanReadable(e.sizeBytes())).setHeader("Memória").setAutoWidth(true);
        entriesGrid.addColumn(CacheEntry::hits).setHeader("Hits").setAutoWidth(true);
        entriesGrid.setItems(entries);
        entriesGrid.setAllRowsVisible(true);
        entriesGrid.setWidthFull();
        return entriesGrid;
    }

    private void refresh() {
        grid.setItems(getCacheStats.executeDetails());
        buildStats();
    }

    private void buildStats() {
        statsArea.removeAll();
        List<CacheDetail> details = getCacheStats.executeDetails();
        long totalEntries = details.stream().mapToLong(CacheDetail::entryCount).sum();
        long totalMemory = details.stream().mapToLong(CacheDetail::memoryBytes).sum();
        long totalHits = details.stream().mapToLong(CacheDetail::hits).sum();

        statsArea.add(new StatCard("Caches ativos", details.size(), VaadinIcon.DATABASE, StatCard.Accent.PRIMARY));
        statsArea.add(new StatCard("Total de entradas", totalEntries, VaadinIcon.RECORDS, StatCard.Accent.SUCCESS));
        statsArea.add(new StatCard("Memória utilizada", humanReadable(totalMemory), VaadinIcon.CHART, StatCard.Accent.WARNING));
        statsArea.add(new StatCard("Total de acessos com sucesso", totalHits, VaadinIcon.BULLSEYE, StatCard.Accent.PRIMARY));
    }

    private void notifySuccess(String message) {
        Notification notification = Notification.show(message);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }

    private static String humanReadable(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        String[] units = {"KB", "MB", "GB", "TB"};
        double value = bytes;
        int unit = -1;
        do {
            value /= 1024;
            unit++;
        } while (value >= 1024 && unit < units.length - 1);
        return String.format("%.1f %s", value, units[unit]);
    }
}

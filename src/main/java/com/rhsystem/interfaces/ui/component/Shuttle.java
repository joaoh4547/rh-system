package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dual-list ("shuttle") selector: items start in the left panel and move to the
 * right panel (the field's value) via the middle buttons. Each panel has a
 * header with caption + item count, a filter field and an empty-state hint.
 */
public class Shuttle<T> extends CustomField<Set<T>> {

    private final ShuttlePanel availablePanel = new ShuttlePanel();
    private final ShuttlePanel chosenPanel = new ShuttlePanel();

    private final Button toChosen;
    private final Button toAvailable;
    private final Button allToChosen;
    private final Button allToAvailable;

    private ItemLabelGenerator<T> labelGenerator = String::valueOf;
    private List<T> allItems = new ArrayList<>();

    public Shuttle(String label) {
        setLabel(label);

        toChosen = moveButton(LucideIcon.chevronRight(), getTranslation("shuttle.move.right"));
        toAvailable = moveButton(LucideIcon.chevronLeft(), getTranslation("shuttle.move.left"));
        allToChosen = moveButton(LucideIcon.chevronsRight(), getTranslation("shuttle.move.all.right"));
        allToAvailable = moveButton(LucideIcon.chevronsLeft(), getTranslation("shuttle.move.all.left"));

        toChosen.addClickListener(e -> move(availablePanel, chosenPanel, availablePanel.list.getSelectedItems()));
        toAvailable.addClickListener(e -> move(chosenPanel, availablePanel, chosenPanel.list.getSelectedItems()));
        allToChosen.addClickListener(e -> move(availablePanel, chosenPanel, new LinkedHashSet<>(availablePanel.items)));
        allToAvailable.addClickListener(e -> move(chosenPanel, availablePanel, new LinkedHashSet<>(chosenPanel.items)));

        availablePanel.list.addSelectionListener(e -> updateButtonStates());
        chosenPanel.list.addSelectionListener(e -> updateButtonStates());

        Div actions = new Div(toChosen, toAvailable, allToChosen, allToAvailable);
        actions.addClassName("shuttle-actions");

        HorizontalLayout layout = new HorizontalLayout(availablePanel.root, actions, chosenPanel.root);
        layout.addClassName("shuttle");
        layout.setWidthFull();
        layout.setSpacing(false);
        add(layout);

        refresh();
    }

    /** Sets captions shown above the available/chosen lists (pass null to omit). */
    public void setCaptions(String availableLabel, String chosenLabel) {
        availablePanel.caption.setText(availableLabel == null ? "" : availableLabel);
        chosenPanel.caption.setText(chosenLabel == null ? "" : chosenLabel);
    }

    public void setItems(Collection<T> items) {
        this.allItems = new ArrayList<>(items);
        availablePanel.items = new ArrayList<>(allItems);
        chosenPanel.items = new ArrayList<>();
        refresh();
    }

    public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
        this.labelGenerator = generator;
        availablePanel.list.setItemLabelGenerator(generator);
        chosenPanel.list.setItemLabelGenerator(generator);
        refresh();
    }

    private void move(ShuttlePanel from, ShuttlePanel to, Set<T> movedItems) {
        if (movedItems.isEmpty()) {
            return;
        }
        from.items = from.items.stream().filter(i -> !movedItems.contains(i)).collect(Collectors.toList());
        to.items.addAll(movedItems);
        refresh();
        updateValue();
    }

    /** Re-applies filters, counts and empty states on both panels. */
    private void refresh() {
        availablePanel.refresh();
        chosenPanel.refresh();
        updateButtonStates();
    }

    private void updateButtonStates() {
        toChosen.setEnabled(!availablePanel.list.getSelectedItems().isEmpty());
        toAvailable.setEnabled(!chosenPanel.list.getSelectedItems().isEmpty());
        allToChosen.setEnabled(!availablePanel.items.isEmpty());
        allToAvailable.setEnabled(!chosenPanel.items.isEmpty());
    }

    private Button moveButton(LucideIcon icon, String tooltip) {
        Button button = new Button(icon);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        button.addClassName("shuttle-move");
        button.setTooltipText(tooltip);
        return button;
    }

    @Override
    public Set<T> getEmptyValue() {
        return new LinkedHashSet<>();
    }

    @Override
    protected Set<T> generateModelValue() {
        return new LinkedHashSet<>(chosenPanel.items);
    }

    @Override
    protected void setPresentationValue(Set<T> newPresentationValue) {
        Set<T> value = newPresentationValue == null ? Set.of() : newPresentationValue;
        chosenPanel.items = allItems.stream().filter(value::contains).collect(Collectors.toList());
        availablePanel.items = allItems.stream().filter(i -> !value.contains(i)).collect(Collectors.toList());
        refresh();
    }

    /** One side of the shuttle: header (caption + count), filter field and list. */
    private class ShuttlePanel {

        private final MultiSelectListBox<T> list = new MultiSelectListBox<>();
        private final Span caption = new Span();
        private final Span count = new Span("0");
        private final TextField filter = new TextField();
        private final Span empty = new Span(getTranslation("shuttle.empty"));
        private final Div root;
        private List<T> items = new ArrayList<>();

        private ShuttlePanel() {
            caption.addClassName("shuttle-caption");
            count.addClassName("shuttle-count");
            Div header = new Div(caption, count);
            header.addClassName("shuttle-panel-header");

            filter.setPlaceholder(getTranslation("shuttle.filter.placeholder"));
            filter.setPrefixComponent(VaadinIcon.SEARCH.create());
            filter.setClearButtonVisible(true);
            filter.addThemeVariants(TextFieldVariant.LUMO_SMALL);
            filter.setValueChangeMode(ValueChangeMode.EAGER);
            filter.addValueChangeListener(e -> refresh());
            filter.addClassName("shuttle-filter");

            empty.addClassName("shuttle-empty");
            list.setWidthFull();
            Div listWrap = new Div(list, empty);
            listWrap.addClassName("shuttle-list");

            root = new Div(header, filter, listWrap);
            root.addClassName("shuttle-panel");
        }

        private void refresh() {
            String term = filter.getValue() == null ? "" : filter.getValue().trim().toLowerCase(Locale.ROOT);
            List<T> visible = term.isEmpty() ? items : items.stream()
                    .filter(i -> labelGenerator.apply(i).toLowerCase(Locale.ROOT).contains(term))
                    .collect(Collectors.toList());
            list.deselectAll();
            list.setItems(visible);
            count.setText(String.valueOf(items.size()));
            empty.setVisible(visible.isEmpty());
        }
    }
}

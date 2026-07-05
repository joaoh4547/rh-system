package com.rhsystem.interfaces.ui.component;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.button.ClickEvent;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.MultiSelectListBox;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Dual-list ("shuttle") selector: items start in the left list and move to the
 * right list (the field's value) via the middle buttons.
 */
public class Shuttle<T> extends CustomField<Set<T>> {

    private final MultiSelectListBox<T> available = new MultiSelectListBox<>();
    private final MultiSelectListBox<T> chosen = new MultiSelectListBox<>();
    private final Span availableCaption = new Span();
    private final Span chosenCaption = new Span();
    private List<T> allItems = new ArrayList<>();

    public Shuttle(String label) {
        setLabel(label);

        available.setWidthFull();
        available.setHeight("220px");
        chosen.setWidthFull();
        chosen.setHeight("220px");

        Button toChosen = iconButton(VaadinIcon.ANGLE_RIGHT, e -> move(available, chosen));
        Button toAvailable = iconButton(VaadinIcon.ANGLE_LEFT, e -> move(chosen, available));
        Button allToChosen = iconButton(VaadinIcon.ANGLE_DOUBLE_RIGHT, e -> moveAll(available, chosen));
        Button allToAvailable = iconButton(VaadinIcon.ANGLE_DOUBLE_LEFT, e -> moveAll(chosen, available));

        VerticalLayout buttons = new VerticalLayout(toChosen, toAvailable, allToChosen, allToAvailable);
        buttons.setPadding(false);
        buttons.setSpacing(true);
        buttons.setAlignItems(FlexComponent.Alignment.CENTER);
        buttons.setWidth(null);

        VerticalLayout availableColumn = column(availableCaption, available);
        VerticalLayout chosenColumn = column(chosenCaption, chosen);

        HorizontalLayout layout = new HorizontalLayout(availableColumn, buttons, chosenColumn);
        layout.setWidthFull();
        layout.setFlexGrow(1, availableColumn);
        layout.setFlexGrow(1, chosenColumn);
        add(layout);
    }

    /** Sets captions shown above the available/chosen lists (pass null to omit). */
    public void setCaptions(String availableLabel, String chosenLabel) {
        availableCaption.setText(availableLabel == null ? "" : availableLabel);
        chosenCaption.setText(chosenLabel == null ? "" : chosenLabel);
    }

    private VerticalLayout column(Span caption, MultiSelectListBox<T> box) {
        VerticalLayout column = new VerticalLayout(caption, box);
        column.setPadding(false);
        column.setSpacing(false);
        column.setWidthFull();
        return column;
    }

    private Button iconButton(VaadinIcon icon, ComponentEventListener<ClickEvent<Button>> handler) {
        Button button = new Button(icon.create(), handler);
        button.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_TERTIARY);
        return button;
    }

    public void setItems(Collection<T> items) {
        this.allItems = new ArrayList<>(items);
        available.setItems(allItems);
        chosen.setItems(List.of());
    }

    public void setItemLabelGenerator(ItemLabelGenerator<T> generator) {
        available.setItemLabelGenerator(generator);
        chosen.setItemLabelGenerator(generator);
    }

    private void move(MultiSelectListBox<T> from, MultiSelectListBox<T> to) {
        Set<T> selected = from.getSelectedItems();
        if (!selected.isEmpty()) {
            transfer(selected, from, to);
        }
    }

    private void moveAll(MultiSelectListBox<T> from, MultiSelectListBox<T> to) {
        Set<T> items = new LinkedHashSet<>(from.getListDataView().getItems().toList());
        if (!items.isEmpty()) {
            transfer(items, from, to);
        }
    }

    private void transfer(Set<T> movedItems, MultiSelectListBox<T> from, MultiSelectListBox<T> to) {
        List<T> fromItems = from.getListDataView().getItems()
                .filter(i -> !movedItems.contains(i)).collect(Collectors.toList());
        List<T> toItems = to.getListDataView().getItems().collect(Collectors.toCollection(ArrayList::new));
        toItems.addAll(movedItems);

        from.deselectAll();
        from.setItems(fromItems);
        to.setItems(toItems);
        updateValue();
    }

    @Override
    public Set<T> getEmptyValue() {
        return new LinkedHashSet<>();
    }

    @Override
    protected Set<T> generateModelValue() {
        return new LinkedHashSet<>(chosen.getListDataView().getItems().toList());
    }

    @Override
    protected void setPresentationValue(Set<T> newPresentationValue) {
        Set<T> value = newPresentationValue == null ? Set.of() : newPresentationValue;
        List<T> chosenList = allItems.stream().filter(value::contains).collect(Collectors.toList());
        List<T> availableList = allItems.stream().filter(i -> !value.contains(i)).collect(Collectors.toList());
        chosen.setItems(chosenList);
        available.setItems(availableList);
    }
}

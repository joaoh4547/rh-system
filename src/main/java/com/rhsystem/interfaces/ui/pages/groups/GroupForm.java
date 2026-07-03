package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.domain.model.Functionality;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.form.Form;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class GroupForm extends Form<GroupFormModel> {

    /**
     * One CheckboxGroup per {@link Functionality.Category}, each holding only a slice of
     * the selection. They can't be bound directly to {@code GroupFormModel::functionalities}
     * (multiple bindings to the same property clobber each other) — selections are merged
     * manually in {@link #syncFunctionalities()} instead.
     */
    private final List<CheckboxGroup<Functionality>> functionalityGroups = new ArrayList<>();

    public GroupForm() {
        super();
        configLayout();
    }

    @Override
    public void setBean(GroupFormModel bean) {
        super.setBean(bean);
        Set<Functionality> selected = bean.getFunctionalities();
        functionalityGroups.forEach(group -> group.setValue(intersect(group, selected)));
    }

    private Set<Functionality> intersect(CheckboxGroup<Functionality> group, Set<Functionality> selected) {
        Set<Functionality> result = new HashSet<>();
        group.getListDataView().getItems().forEach(item -> {
            if (selected.contains(item)) {
                result.add(item);
            }
        });
        return result;
    }

    private void syncFunctionalities() {
        Set<Functionality> merged = new HashSet<>();
        functionalityGroups.forEach(group -> merged.addAll(group.getValue()));
        getBean().setFunctionalities(merged);
        log.info(merged.toString());
    }

    private void configLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);


        FormLayout main = formLayout();

        main.add(requiredTextField(getTranslation("field.name"), "name", getTranslation("field.name.placeholder")), 2);
        main.add(textArea(getTranslation("field.description"), "description"), 2);
        HorizontalLayout checks = new HorizontalLayout();
        checks.setSpacing(false);
        checks.setPadding(false);

        checks.add(checkbox(getTranslation("field.ativo"), "active"));
        checks.add(checkbox(getTranslation("field.admin"), "admin"));

        main.add(checks);

        layout.add(main);

        layout.add(createFunctionalities());

        layout.setHeight("750px");
        add(layout);
    }

    private Component createFunctionalities() {
        var tabs = tabSheet();
        var tab = new Tab(LucideIcon.functionalities(), new Span(getTranslation("functionalities.label")));
        tab.getStyle().setGap("10px");

        var layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setPadding(false);

        for (var func : Functionality.getFunctionalityByCategory().entrySet()) {
            if (layout.getComponentCount() > 0) {
                layout.add(new Hr());
            }

            Collection<Functionality> items = func.getValue();

            CheckboxGroup<Functionality> checkboxGroup = new CheckboxGroup<>();
            checkboxGroup.setLabel(getTranslation(func.getKey().getLabel()));
            checkboxGroup.setItems(items);
            checkboxGroup.setItemLabelGenerator(i -> getTranslation(i.getLabel()));

            checkboxGroup.addValueChangeListener(e -> {
                syncFunctionalities();
            });

            functionalityGroups.add(checkboxGroup);
            layout.add(checkboxGroup);
        }

        tabs.add(tab, layout);
        tabs.setHeightFull();
        return tabs;
    }
}

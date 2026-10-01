package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.domain.model.parameters.ParameterType;
import com.rhsystem.interfaces.ui.form.Form;
import com.vaadin.flow.component.Component;

public class ParameterForm extends Form<ParameterFormModel> {


    public ParameterForm(ParameterFormModel value) {
        super(value);
        configureLayout();
    }


    private void configureLayout() {
        var layout = formLayout(1);
        layout.add(createNameField());
        layout.add(createValueField());
        add(layout);
    }

    private Component createValueField() {
        var obj = getBean();
        final var label = getTranslation("value.label");
        final var property = "value";
        return switch (obj.getType()) {
            case TEXT -> textField(label, property);
            case NUMBER -> numberField(label, property);
            case SECRET -> passwordField(label, property);
            case BOOLEAN -> checkbox(label, property);
            case DATE -> datePicker(label, property);
        };
    }

    private Component createNameField() {
        var comp = requiredTextField(getTranslation("field.name"), "name", getTranslation("error.name.required"));
        comp.setReadOnly(true);
        return comp;
    }

}

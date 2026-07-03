package com.rhsystem.interfaces.ui.form;

import com.rhsystem.utils.Reflections;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.BigDecimalField;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.timepicker.TimePicker;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Reusable base for forms bound to a bean via {@link Binder}.
 *
 * @param <T> type of the bean being edited
 */
@Getter
public abstract class Form<T> extends Div {

    protected final Binder<T> binder;
    private T bean;

    protected Form(Class<T> beanType) {
        this.binder = new BeanValidationBinder<>(beanType);
        initialize();
    }


    public Class<T> getType() {
        return (Class<T>) Reflections.getGenericType(getClass(),0);
    }

    protected Form() {
        this.binder = new BeanValidationBinder<>(getType());
        initialize();
    }

    private void initialize() {
        setWidthFull();
        addClassName("rh-form");
    }

    /* ===================== Binder / bean ===================== */

    public void setBean(T bean) {
        this.bean = bean;
        binder.readBean(bean);
    }

    public boolean isValid() {
        return binder.validate().isOk();
    }

    public boolean writeBeanIfValid(T target) {
        return binder.writeBeanIfValid(target);
    }

    public void writeBean(T target) throws ValidationException {
        binder.writeBean(target);
    }

    public Optional<T> ifValid(T target) {
        return writeBeanIfValid(target) ? Optional.of(target) : Optional.empty();
    }

    /* ===================== Bind helpers ===================== */

    protected <C extends Component & HasValue<?, V>, V> C bind(C field, String property) {
        binder.forField(field).bind(property);
        return field;
    }

    protected <C extends Component & HasValue<?, V>, V> C bindRequired(C field, String property, String error) {
        binder.forField(field).asRequired(error).bind(property);
        return field;
    }

    /* ===================== Field factories ===================== */

    protected TextField textField(String label) {
        return configure(new TextField(label));
    }

    protected TextField textField(String label, String property) {
        return bind(textField(label), property);
    }

    protected TextField requiredTextField(String label, String property, String error) {
        TextField f = textField(label);
        f.setRequiredIndicatorVisible(true);
        return bindRequired(f, property, error);
    }

    protected TextArea requiredTextArea(String label, String property, String error) {
        TextArea f = configure(new TextArea(label));
        f.setRequiredIndicatorVisible(true);
        return bindRequired(f, property, error);
    }

    protected EmailField emailField(String label) {
        EmailField f = configure(new EmailField(label));
        f.setClearButtonVisible(true);
        return f;
    }

    protected EmailField emailField(String label, String property) {
        return bind(emailField(label), property);
    }

    protected EmailField requiredEmailField(String label, String property, String error) {
        EmailField f = emailField(label);
        f.setRequiredIndicatorVisible(true);
        return bindRequired(f, property, error);
    }

    protected PasswordField passwordField(String label) {
        return configure(new PasswordField(label));
    }

    protected PasswordField passwordField(String label, String property) {
        return bind(passwordField(label), property);
    }

    protected TextArea textArea(String label) {
        return configure(new TextArea(label));
    }

    protected TextArea textArea(String label, String property) {
        return bind(textArea(label), property);
    }

    protected IntegerField integerField(String label) {
        return configure(new IntegerField(label));
    }

    protected IntegerField integerField(String label, String property) {
        return bind(integerField(label), property);
    }

    protected NumberField numberField(String label) {
        return configure(new NumberField(label));
    }

    protected NumberField numberField(String label, String property) {
        return bind(numberField(label), property);
    }

    protected BigDecimalField bigDecimalField(String label) {
        return configure(new BigDecimalField(label));
    }

    protected BigDecimalField bigDecimalField(String label, String property) {
        return bind(bigDecimalField(label), property);
    }

    protected DatePicker datePicker(String label) {
        return configure(new DatePicker(label));
    }

    protected DatePicker datePicker(String label, String property) {
        return bind(datePicker(label), property);
    }

    protected TimePicker timePicker(String label) {
        return configure(new TimePicker(label));
    }

    protected TimePicker timePicker(String label, String property) {
        return bind(timePicker(label), property);
    }

    protected DateTimePicker dateTimePicker(String label) {
        return configure(new DateTimePicker(label));
    }

    protected DateTimePicker dateTimePicker(String label, String property) {
        return bind(dateTimePicker(label), property);
    }

    protected Checkbox checkbox(String label) {
        return new Checkbox(label);
    }

    protected Checkbox checkbox(String label, String property) {
        return bind(checkbox(label), property);
    }

    protected <E> ComboBox<E> comboBox(String label, Collection<E> items) {
        ComboBox<E> cb = configure(new ComboBox<>(label));
        cb.setItems(items);
        return cb;
    }

    @SafeVarargs
    protected final <E> ComboBox<E> comboBox(String label, E... items) {
        ComboBox<E> cb = configure(new ComboBox<>(label));
        cb.setItems(items);
        return cb;
    }

    protected <E> ComboBox<E> comboBox(String label, String property, Collection<E> items) {
        return bind(comboBox(label, items), property);
    }

    protected <E> ComboBox<E> comboBox(String label, String property, Collection<E> items,
                                       com.vaadin.flow.component.ItemLabelGenerator<E> labelGenerator) {
        ComboBox<E> cb = comboBox(label, items);
        cb.setItemLabelGenerator(labelGenerator);
        return bind(cb, property);
    }

    protected <E> Select<E> select(String label, Collection<E> items) {
        Select<E> sel = configure(new Select<>());
        sel.setLabel(label);
        sel.setItems(items);
        return sel;
    }

    protected <E> Select<E> select(String label, String property, Collection<E> items) {
        return bind(select(label, items), property);
    }

    protected <E> RadioButtonGroup<E> radioGroup(String label, Collection<E> items) {
        RadioButtonGroup<E> rg = new RadioButtonGroup<>();
        rg.setLabel(label);
        rg.setItems(items);
        return rg;
    }

    protected <E> RadioButtonGroup<E> radioGroup(String label, String property, Collection<E> items) {
        return bind(radioGroup(label, items), property);
    }

    /* ===================== Utility helpers ===================== */

    protected <C extends HasValue<?, ?>> C required(C field) {
        field.setRequiredIndicatorVisible(true);
        return field;
    }

    protected <C extends Component> C with(C field, Consumer<C> config) {
        config.accept(field);
        return field;
    }

    private <C extends Component> C configure(C field) {
        if (field instanceof com.vaadin.flow.component.HasSize hasSize) {
            hasSize.setWidthFull();
        }
        return field;
    }

    /* ===================== Layout helpers ===================== */

    /** Responsive FormLayout (1 column when narrow, 2 from 480px). */
    protected FormLayout formLayout(Component... fields) {
        FormLayout form = new FormLayout(fields);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", 2));
        return form;
    }

    /** FormLayout with a fixed number of columns from 480px. */
    protected FormLayout formLayout(int columns, Component... fields) {
        FormLayout form = new FormLayout(fields);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", columns));
        return form;
    }

    /** Creates a full-width {@link TabSheet} for form tabs. */
    protected TabSheet tabSheet() {
        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        return tabs;
    }
}

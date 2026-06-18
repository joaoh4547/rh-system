package com.rhsystem.interfaces.ui.form;

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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base reutilizável para formulários ligados a um bean via {@link Binder}.
 *
 * <p>Além de encapsular o binder e o ciclo de leitura/escrita, oferece um
 * conjunto de <b>fábricas de campos</b> (texto, email, senha, número, data,
 * combo, checkbox...) e helpers de <b>bind</b> e <b>layout</b> para reduzir
 * a repetição na criação de telas de cadastro.</p>
 *
 * <pre>{@code
 * class UsuarioForm extends Form<UsuarioModel> {
 *     UsuarioForm() {
 *         super(UsuarioModel.class);
 *         var nome  = textField("Nome", "nome");                 // cria + bind
 *         var email = requiredEmailField("Email", "email", "Informe o email");
 *         var ativo = checkbox("Ativo", "ativo");
 *         add(formLayout(nome, email, ativo));
 *     }
 * }
 * }</pre>
 *
 * <p><b>Observação:</b> os métodos {@code *(label, property)} usam binding por
 * nome de propriedade e exigem o construtor {@link #Form(Class)} (com tipo do
 * bean). Para binders sem tipo, use {@link #getBinder()} e faça o bind por
 * getter/setter.</p>
 *
 * @param <T> tipo do bean editado
 */
public abstract class Form<T> extends Div {

    protected final Binder<T> binder;
    private T bean;

    protected Form(Class<T> beanType) {
        this.binder = new BeanValidationBinder<>(beanType);
        inicializar();
    }

    protected Form() {
        this.binder = new Binder<>();
        inicializar();
    }

    private void inicializar() {
        setWidthFull();
        addClassName("rh-form");
    }

    /* ===================== Binder / bean ===================== */

    public Binder<T> getBinder() {
        return binder;
    }

    public T getBean() {
        return bean;
    }

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

    /* ===================== Helpers de bind ===================== */

    /** Liga um campo a uma propriedade do bean (binding por nome). */
    protected <C extends Component & HasValue<?, V>, V> C bind(C field, String property) {
        binder.forField(field).bind(property);
        return field;
    }

    /** Liga um campo obrigatório a uma propriedade, com mensagem de erro. */
    protected <C extends Component & HasValue<?, V>, V> C bindRequired(C field, String property, String erro) {
        binder.forField(field).asRequired(erro).bind(property);
        return field;
    }

    /* ===================== Fábricas de campos ===================== */

    protected TextField textField(String label) {
        return configurar(new TextField(label));
    }

    protected TextField textField(String label, String property) {
        return bind(textField(label), property);
    }

    protected TextField requiredTextField(String label, String property, String erro) {
        TextField f = textField(label);
        f.setRequiredIndicatorVisible(true);
        return bindRequired(f, property, erro);
    }

    protected EmailField emailField(String label) {
        EmailField f = configurar(new EmailField(label));
        f.setClearButtonVisible(true);
        return f;
    }

    protected EmailField emailField(String label, String property) {
        return bind(emailField(label), property);
    }

    protected EmailField requiredEmailField(String label, String property, String erro) {
        EmailField f = emailField(label);
        f.setRequiredIndicatorVisible(true);
        return bindRequired(f, property, erro);
    }

    protected PasswordField passwordField(String label) {
        return configurar(new PasswordField(label));
    }

    protected PasswordField passwordField(String label, String property) {
        return bind(passwordField(label), property);
    }

    protected TextArea textArea(String label) {
        return configurar(new TextArea(label));
    }

    protected TextArea textArea(String label, String property) {
        return bind(textArea(label), property);
    }

    protected IntegerField integerField(String label) {
        return configurar(new IntegerField(label));
    }

    protected IntegerField integerField(String label, String property) {
        return bind(integerField(label), property);
    }

    protected NumberField numberField(String label) {
        return configurar(new NumberField(label));
    }

    protected NumberField numberField(String label, String property) {
        return bind(numberField(label), property);
    }

    protected BigDecimalField bigDecimalField(String label) {
        return configurar(new BigDecimalField(label));
    }

    protected BigDecimalField bigDecimalField(String label, String property) {
        return bind(bigDecimalField(label), property);
    }

    protected DatePicker datePicker(String label) {
        return configurar(new DatePicker(label));
    }

    protected DatePicker datePicker(String label, String property) {
        return bind(datePicker(label), property);
    }

    protected TimePicker timePicker(String label) {
        return configurar(new TimePicker(label));
    }

    protected TimePicker timePicker(String label, String property) {
        return bind(timePicker(label), property);
    }

    protected DateTimePicker dateTimePicker(String label) {
        return configurar(new DateTimePicker(label));
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
        ComboBox<E> cb = configurar(new ComboBox<>(label));
        cb.setItems(items);
        return cb;
    }

    @SafeVarargs
    protected final <E> ComboBox<E> comboBox(String label, E... items) {
        ComboBox<E> cb = configurar(new ComboBox<>(label));
        cb.setItems(items);
        return cb;
    }

    protected <E> ComboBox<E> comboBox(String label, String property, Collection<E> items) {
        return bind(comboBox(label, items), property);
    }

    protected <E> ComboBox<E> comboBox(String label, String property, Collection<E> items,
                                       com.vaadin.flow.component.ItemLabelGenerator<E> rotulo) {
        ComboBox<E> cb = comboBox(label, items);
        cb.setItemLabelGenerator(rotulo);
        return bind(cb, property);
    }

    protected <E> Select<E> select(String label, Collection<E> items) {
        Select<E> sel = configurar(new Select<>());
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

    /* ===================== Helpers utilitários ===================== */

    /** Marca o campo como obrigatório (apenas visual) e o retorna. */
    protected <C extends HasValue<?, ?>> C required(C field) {
        field.setRequiredIndicatorVisible(true);
        return field;
    }

    /** Aplica uma configuração extra ao campo recém-criado, de forma fluente. */
    protected <C extends Component> C with(C field, Consumer<C> config) {
        config.accept(field);
        return field;
    }

    private <C extends Component> C configurar(C field) {
        if (field instanceof com.vaadin.flow.component.HasSize hasSize) {
            hasSize.setWidthFull();
        }
        return field;
    }

    /* ===================== Helpers de layout ===================== */

    /** FormLayout responsivo (1 coluna no estreito, 2 a partir de 480px). */
    protected FormLayout formLayout(Component... fields) {
        FormLayout form = new FormLayout(fields);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", 2));
        return form;
    }

    /** FormLayout com número fixo de colunas a partir de 480px. */
    protected FormLayout formLayout(int colunas, Component... fields) {
        FormLayout form = new FormLayout(fields);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", colunas));
        return form;
    }

    /** Cria um {@link TabSheet} já com largura total para abas do formulário. */
    protected TabSheet tabSheet() {
        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        return tabs;
    }
}

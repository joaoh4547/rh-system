package com.rhsystem.interfaces.ui.form;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;

import java.util.Optional;

/**
 * Base reutilizável para formulários ligados a um bean via {@link Binder}.
 *
 * <p>Encapsula o binder (com validação Bean Validation quando o tipo é informado),
 * a leitura/escrita do bean e helpers de layout. As subclasses criam os campos,
 * fazem os binds e adicionam o conteúdo no próprio construtor.</p>
 *
 * <pre>{@code
 * class UsuarioForm extends Form<UsuarioModel> {
 *     private final TextField nome = new TextField("Nome");
 *     UsuarioForm() {
 *         super(UsuarioModel.class);
 *         getBinder().forField(nome).asRequired("Informe o nome").bind("nome");
 *         add(formLayout(nome));
 *     }
 * }
 * }</pre>
 *
 * @param <T> tipo do bean editado
 */
public abstract class Form<T> extends Div {

    protected final Binder<T> binder;
    private T bean;

    /** Cria o form com validação Bean Validation (JSR-380) para o tipo informado. */
    protected Form(Class<T> beanType) {
        this.binder = new BeanValidationBinder<>(beanType);
        inicializar();
    }

    /** Cria o form com um binder simples (sem validação automática por anotações). */
    protected Form() {
        this.binder = new Binder<>();
        inicializar();
    }

    private void inicializar() {
        setWidthFull();
        addClassName("rh-form");
    }

    public Binder<T> getBinder() {
        return binder;
    }

    public T getBean() {
        return bean;
    }

    /** Carrega um bean nos campos (cópia de leitura). Use {@code null} para limpar. */
    public void setBean(T bean) {
        this.bean = bean;
        binder.readBean(bean);
    }

    public boolean isValid() {
        return binder.validate().isOk();
    }

    /** Escreve os valores no alvo apenas se o formulário estiver válido. */
    public boolean writeBeanIfValid(T target) {
        return binder.writeBeanIfValid(target);
    }

    /** Escreve os valores no alvo, lançando exceção se inválido. */
    public void writeBean(T target) throws ValidationException {
        binder.writeBean(target);
    }

    /** Retorna o alvo preenchido se válido; vazio caso contrário. */
    public Optional<T> ifValid(T target) {
        return writeBeanIfValid(target) ? Optional.of(target) : Optional.empty();
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
}

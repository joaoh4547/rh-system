package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.application.dto.parameter.UpdateParameter;
import com.rhsystem.application.dto.parameter.UpdateParameterCommand;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterType;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
import com.rhsystem.domain.model.security.ValueEncoder;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.rhsystem.interfaces.ui.shared.ValidationNotifier;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

/**
 * Edit Parameter dialog.
 *
 * <p>Prototype bean: use cases are injected by Spring; the page only supplies
 * runtime data via {@link #edit(Parameter, Runnable)}.
 */
@SpringComponent
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class ParameterFormDialog extends FormDialog<ParameterFormModel> {

    private final ValueEncoder encoder;
    private final ParameterValueConverter converter;
    private final UpdateParameter updateParameter;

    private Parameter editing;
    private Runnable onSaved;

    /**
     * Assembles the dialog for editing the given parameter.
     */
    public ParameterFormDialog edit(Parameter editing, Runnable onSaved) {
        this.editing = editing;
        this.onSaved = onSaved;

        init("form.parameter.editing", new ParameterForm(ParameterFormModel.of(editing, converter)));
        actions(
                FormDialogAction.cancel(),
                FormDialogAction.primary(getTranslation("action.save"), this::save).icon(LucideIcon.check()));
        return this;
    }

    private void save() {
        ParameterFormModel obj = getForm().getBean();
        if (!getForm().writeBeanIfValid(obj)) {
            return;
        }

        try {
            var command = new UpdateParameterCommand(editing.getParameter(), obj.getName(), resolveValue());
            updateParameter.execute(command);
            onSaved.run();
            close();
        } catch (ValidationException ve) {
            ValidationNotifier.show(this::getTranslation, ve);
        }
    }

    private String resolveValue() {
        ParameterFormModel model = getForm().getBean();
        String value = String.valueOf(model.getValue());
        if (model.getType() == ParameterType.SECRET) {
            value = encoder.encode(value);
        }
        return value;
    }
}

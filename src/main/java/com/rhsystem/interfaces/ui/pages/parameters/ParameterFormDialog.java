package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.application.dto.parameter.UpdateParameter;
import com.rhsystem.application.dto.parameter.UpdateParameterCommand;
import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterType;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
import com.rhsystem.domain.model.security.ValueDecoder;
import com.rhsystem.domain.model.security.ValueEncoder;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.rhsystem.interfaces.ui.shared.ValidationNotifier;

public class ParameterFormDialog extends FormDialog<ParameterFormModel> {

    private final Parameter editing;
    private final Runnable onSave;
    private final ValueEncoder encoder;
    private final UpdateParameter updateParameter;

    public ParameterFormDialog(Parameter editing, Runnable onSave, ValueEncoder encoder, ParameterValueConverter converter, ValueDecoder decoder, UpdateParameter updateParameter) {
        super("form.parameter.editing", new ParameterForm(ParameterFormModel.of(editing, converter)));
        this.editing = editing;
        this.onSave = onSave;
        this.encoder = encoder;
        this.updateParameter = updateParameter;

        actions(FormDialogAction.cancel(), FormDialogAction.primary(getTranslation("action.save"), this::save).icon(LucideIcon.check()));
    }


    private void save() {
        ParameterFormModel obj = getForm().getBean();
        if (!getForm().writeBeanIfValid(obj)) {
            return;
        }

        try {
            var command = new UpdateParameterCommand(editing.getParameter(), obj.getName(), resolveValue());
            updateParameter.execute(command);
            onSave.run();
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

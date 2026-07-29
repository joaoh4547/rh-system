package com.rhsystem.interfaces.ui.pages.parameters;

import com.rhsystem.domain.model.parameters.Parameter;
import com.rhsystem.domain.model.parameters.ParameterType;
import com.rhsystem.domain.model.parameters.ParameterValueConverter;
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

    public ParameterFormDialog(Parameter editing, Runnable onSave, ValueEncoder encoder, ParameterValueConverter converter) {
        super("form.parameter.editing", new ParameterForm(ParameterFormModel.of(editing, converter)));
        this.editing = editing;
        this.onSave = onSave;
        this.encoder = encoder;

        actions(FormDialogAction.cancel(), FormDialogAction.primary(getTranslation("action.save"), this::save).icon(LucideIcon.check()));
    }


    private void save() {
        ParameterFormModel obj = getForm().getBean();
        if (getForm().writeBeanIfValid(obj)) {
            return;
        }

        try {
            //TODO: implement update
            var command = new UpdateParameterCommand(editing.getParameter(), obj.name(), resolveValue());
            onSave.run();
        } catch (ValidationException ve) {
            ValidationNotifier.show(this::getTranslation, ve);
        }
    }

    private String resolveValue() {
        ParameterFormModel model = getForm().getBean();
        String value = String.valueOf(model.value());
        if (model.type() == ParameterType.SECRET) {
            value = encoder.encode(value);
        }
        return value;
    }


}

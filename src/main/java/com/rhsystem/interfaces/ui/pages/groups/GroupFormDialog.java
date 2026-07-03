package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.form.Form;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.vaadin.flow.component.icon.VaadinIcon;

public class GroupFormDialog extends FormDialog<GroupFormModel> {

    public GroupFormDialog(Group editing) {
        super(editing == null ? "form.group.title.new" : "form.group.title.edit", new GroupForm());
        width("880px");

        getForm().setBean(new GroupFormModel());

        actions(FormDialogAction.cancel(getTranslation("action.cancel")), FormDialogAction.primary(getTranslation("action.save"), () -> {
        }).icon(LucideIcon.check()));

    }

    @Override
    protected boolean startMaximized() {
        return true;
    }
}

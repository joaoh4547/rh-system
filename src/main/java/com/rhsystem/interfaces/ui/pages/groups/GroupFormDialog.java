package com.rhsystem.interfaces.ui.pages.groups;

import com.rhsystem.application.dto.group.CreateGroupCommand;
import com.rhsystem.application.dto.group.UpdateGroupCommand;
import com.rhsystem.application.usecase.group.CreateGroup;
import com.rhsystem.application.usecase.group.UpdateGroup;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.interfaces.ui.component.LucideIcon;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.rhsystem.interfaces.ui.shared.ValidationNotifier;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class GroupFormDialog extends FormDialog<GroupFormModel> {


    private final Runnable onSave;

    private final CreateGroup createGroup;
    private final UpdateGroup updateGroup;
    private final Group editing;


    public GroupFormDialog(Group editing, CreateGroup createGroup, UpdateGroup updateGroup, Runnable onSave) {
        super(editing == null ? "form.group.title.new" : "form.group.title.edit", new GroupForm());
        this.onSave = onSave;
        this.createGroup = createGroup;
        this.updateGroup = updateGroup;
        this.editing = editing;
        width("880px");

        getForm().setBean(editing == null ? new GroupFormModel() : GroupFormModel.of(editing));

        actions(FormDialogAction.cancel(getTranslation("action.cancel")), FormDialogAction.primary(getTranslation("action.save"), this::save).icon(LucideIcon.check()));

    }

    public void save() {
        var model = getForm().getBean();

        if (!getForm().writeBeanIfValid(model)) {
            return;
        }

        try {
            if (editing == null) {
                var command = new CreateGroupCommand(
                        model.getName(),
                        model.getDescription(),
                        model.isActive(),
                        model.isAdmin(),
                        model.getFunctionalities()
                );

                createGroup.execute(command);
            } else {
                var command = new UpdateGroupCommand(
                        editing.getId(),
                        model.getName(),
                        model.getDescription(),
                        model.isActive(),
                        model.isAdmin(),
                        model.getFunctionalities()
                );

                updateGroup.execute(command);
            }

            notify("form.group.saved", true);
            onSave.run();
            close();
        } catch (ValidationException ex) {
            ValidationNotifier.show(this::getTranslation, ex);
        }
    }
}

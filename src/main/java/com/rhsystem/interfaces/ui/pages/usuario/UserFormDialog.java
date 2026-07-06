package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.UpdateUserCommand;
import com.rhsystem.application.usecase.usuario.CreateUser;
import com.rhsystem.application.usecase.usuario.UpdateUser;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.rhsystem.interfaces.ui.shared.ValidationNotifier;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Create/edit User dialog, built on top of {@link FormDialog} and {@link UserForm}.
 */
public class UserFormDialog extends FormDialog<UserFormModel> {

    private final CreateUser createUser;
    private final UpdateUser updateUser;
    private final User editing;
    private final Runnable onSaved;
    private final UserForm form;

    public UserFormDialog(CreateUser createUser, UpdateUser updateUser,
                          User editing, List<Group> availableGroups, Runnable onSaved) {
        super(editing == null ? "form.user.title.new" : "form.user.title.edit",
                new UserForm(editing != null, availableGroups));
        this.createUser = createUser;
        this.updateUser = updateUser;
        this.editing = editing;
        this.onSaved = onSaved;
        this.form = (UserForm) getForm();

        width("820px");
        actions(
                FormDialogAction.cancel(getTranslation("action.cancel")),
                FormDialogAction.primary(getTranslation("action.save"), this::save));

        getForm().setBean(editing == null ? new UserFormModel() : UserFormModel.from(editing));
    }

    private void save() {
        UserFormModel model = getForm().getBean();
        if (!getForm().writeBeanIfValid(model)) {
            return;
        }
        try {
            AddressDTO address = new AddressDTO(
                    model.getStreet(), model.getNeighborhood(),
                    model.getStreetNumber(), model.getComplement(), model.getPostalCode());
            var groupIds = model.getGroups().stream().map(Group::getId).collect(Collectors.toSet());

            if (editing == null) {
                createUser.execute(new CreateUserCommand(
                        model.getFirstName(), model.getLastName(), model.getEmail(),
                        model.getCpf(), model.getRg(), address, form.getAttachments(), groupIds));
                notify("form.user.saved.created", true);
            } else {
                updateUser.execute(new UpdateUserCommand(
                        editing.getId(), model.getFirstName(), model.getLastName(), model.getEmail(),
                        model.getCpf(), model.getRg(), model.getStatus(), address, groupIds));
                notify("form.user.saved.updated", true);
            }
            onSaved.run();
            close();
        } catch (ValidationException ex) {
            ValidationNotifier.show(this::getTranslation, ex);
        }
    }


}

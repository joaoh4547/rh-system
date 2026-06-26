package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.UpdateUserCommand;
import com.rhsystem.application.exception.BusinessException;
import com.rhsystem.application.usecase.usuario.CreateUser;
import com.rhsystem.application.usecase.usuario.UpdateUser;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

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
                             User editing, Runnable onSaved) {
        super(editing == null ? "form.user.title.new" : "form.user.title.edit", new UserForm(editing != null));
        this.createUser = createUser;
        this.updateUser = updateUser;
        this.editing    = editing;
        this.onSaved    = onSaved;
        this.form       = (UserForm) getForm();

        setHeaderTitle(getTranslation(editing == null ? "form.user.title.new" : "form.user.title.edit"));
        width("680px");
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

            if (editing == null) {
                createUser.execute(new CreateUserCommand(
                        model.getFirstName(), model.getLastName(), model.getEmail(),
                        model.getCpf(), model.getRg(), address, form.getAttachments()));
                notify("form.user.saved.created", true);
            } else {
                updateUser.execute(new UpdateUserCommand(
                        editing.getId(), model.getFirstName(), model.getLastName(), model.getEmail(),
                        model.getCpf(), model.getRg(), model.getStatus(), address));
                notify("form.user.saved.updated", true);
            }
            onSaved.run();
            close();
        } catch (BusinessException ex) {
            notify(ex.getMessage(), false);
        }
    }

    private void notify(String key, boolean success) {
        Notification.show(getTranslation(key)).addThemeVariants(
                success ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
    }
}

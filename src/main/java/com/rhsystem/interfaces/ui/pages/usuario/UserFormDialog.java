package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.application.dto.usuario.AddressDTO;
import com.rhsystem.application.dto.usuario.CreateUserCommand;
import com.rhsystem.application.dto.usuario.UpdateUserCommand;
import com.rhsystem.application.usecase.group.ListGroups;
import com.rhsystem.application.usecase.usuario.CreateUser;
import com.rhsystem.application.usecase.usuario.UpdateUser;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.validation.ValidationException;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.rhsystem.interfaces.ui.shared.ValidationNotifier;
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

import java.util.stream.Collectors;

/**
 * Create/edit User dialog, built on top of {@link FormDialog} and {@link UserForm}.
 *
 * <p>Prototype bean: use cases are injected by Spring; the page only supplies
 * runtime data via {@link #edit(User, Runnable)}.
 */
@SpringComponent
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class UserFormDialog extends FormDialog<UserFormModel> {

    private final CreateUser createUser;
    private final UpdateUser updateUser;
    private final ListGroups listGroups;

    private User editing;
    private Runnable onSaved;
    private UserForm form;

    /**
     * Assembles the dialog for creating ({@code editing == null}) or editing a user.
     */
    public UserFormDialog edit(@Nullable User editing, Runnable onSaved) {
        this.editing = editing;
        this.onSaved = onSaved;
        this.form = new UserForm(editing != null, listGroups.executeActive());

        init(editing == null ? "form.user.title.new" : "form.user.title.edit", form);
        width("820px");
        actions(
                FormDialogAction.cancel(getTranslation("action.cancel")),
                FormDialogAction.primary(getTranslation("action.save"), this::save));

        form.setBean(editing == null ? new UserFormModel() : UserFormModel.from(editing));
        return this;
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

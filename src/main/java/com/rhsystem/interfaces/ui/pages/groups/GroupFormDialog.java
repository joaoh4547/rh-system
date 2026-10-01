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
import com.vaadin.flow.spring.annotation.SpringComponent;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;

/**
 * Create/edit Group dialog.
 *
 * <p>Prototype bean: use cases are injected by Spring; the page only supplies
 * runtime data via {@link #edit(Group, Runnable)}.
 */
@SpringComponent
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
@RequiredArgsConstructor
public class GroupFormDialog extends FormDialog<GroupFormModel> {

    private final CreateGroup createGroup;
    private final UpdateGroup updateGroup;

    private Group editing;
    private Runnable onSaved;

    /**
     * Assembles the dialog for creating ({@code editing == null}) or editing a group.
     */
    public GroupFormDialog edit(@Nullable Group editing, Runnable onSaved) {
        this.editing = editing;
        this.onSaved = onSaved;

        init(editing == null ? "form.group.title.new" : "form.group.title.edit", new GroupForm());
        width("880px");
        actions(
                FormDialogAction.cancel(getTranslation("action.cancel")),
                FormDialogAction.primary(getTranslation("action.save"), this::save).icon(LucideIcon.check()));

        getForm().setBean(editing == null ? new GroupFormModel() : GroupFormModel.of(editing));
        return this;
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
            onSaved.run();
            close();
        } catch (ValidationException ex) {
            ValidationNotifier.show(this::getTranslation, ex);
        }
    }
}

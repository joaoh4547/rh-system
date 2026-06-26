package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.application.dto.usuario.DocumentUpload;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.interfaces.ui.component.DocumentField;
import com.rhsystem.interfaces.ui.form.Form;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Create/edit User form built on top of {@link Form}, using field factories and bind helpers.
 */
public class UserForm extends Form<UserFormModel> {

    private final DocumentField cpf;
    private final DocumentField rg;
    private final Map<String, byte[]> fileContents = new LinkedHashMap<>();
    private final Map<String, String> fileMimeTypes = new LinkedHashMap<>();
    private final Upload upload = new Upload();

    public UserForm(boolean editMode) {
        super(UserFormModel.class);

        cpf = new DocumentField(getTranslation("field.cpf"), DocumentField.Type.CPF);
        rg  = new DocumentField(getTranslation("field.rg"),  DocumentField.Type.RG);

        TextField firstName = requiredTextField(getTranslation("field.first.name"), "firstName", getTranslation("field.first.name.placeholder"));
        TextField lastName  = requiredTextField(getTranslation("field.last.name"),  "lastName",  getTranslation("field.last.name.placeholder"));
        configureDocumentFields();
        configureUpload();
        FormLayout personalData = formLayout(firstName, lastName, cpf, rg);

        EmailField email = requiredEmailField(getTranslation("field.email"), "email", getTranslation("field.email.placeholder"));
        FormLayout accessData;
        if (editMode) {
            var status = comboBox(getTranslation("field.status"), "status",
                    List.of(UserStatus.values()), UserStatus::getLabel);
            accessData = formLayout(email, status);
        } else {
            accessData = formLayout(email);
        }

        FormLayout address = formLayout(
                textField(getTranslation("field.street"),        "street"),
                textField(getTranslation("field.street.number"), "streetNumber"),
                textField(getTranslation("field.neighborhood"),  "neighborhood"),
                textField(getTranslation("field.complement"),    "complement"),
                textField(getTranslation("field.postal.code"),   "postalCode"));

        TabSheet tabs = tabSheet();
        tabs.add(getTranslation("form.user.tab.personal"), personalData);
        tabs.add(getTranslation("form.user.tab.access"),   accessData);
        tabs.add(getTranslation("form.user.tab.address"),  address);

        if (!editMode) {
            upload.setDropLabel(new Span(getTranslation("form.user.documents.drop")));
            upload.setWidthFull();
            tabs.add(getTranslation("form.user.tab.documents"), upload);
        }
        add(tabs);
    }

    private void configureUpload() {
        ByteArrayOutputStream[] currentStream = {null};

        upload.setReceiver((fileName, mimeType) -> {
            fileMimeTypes.put(fileName, mimeType);
            currentStream[0] = new ByteArrayOutputStream();
            return currentStream[0];
        });

        upload.addSucceededListener(e -> {
            if (currentStream[0] != null) {
                fileContents.put(e.getFileName(), currentStream[0].toByteArray());
            }
        });
    }

    /** CPF/RG use masked fields; the binder stores only digits in the model. */
    private void configureDocumentFields() {
        cpf.setWidthFull();
        cpf.setRequiredIndicatorVisible(true);
        rg.setWidthFull();
        rg.setRequiredIndicatorVisible(true);

        getBinder().forField(cpf).asRequired("CPF is required")
                .withConverter(v -> v == null ? "" : v.replaceAll("\\D", ""), d -> d == null ? "" : d)
                .bind(UserFormModel::getCpf, UserFormModel::setCpf);

        getBinder().forField(rg).asRequired("RG is required")
                .withConverter(v -> v == null ? "" : v.replaceAll("[^A-Za-z0-9]", ""), d -> d == null ? "" : d)
                .bind(UserFormModel::getRg, UserFormModel::setRg);
    }

    /** Returns the attachments uploaded (create mode only). */
    public List<DocumentUpload> getAttachments() {
        List<DocumentUpload> attachments = new ArrayList<>();
        fileContents.forEach((name, content) ->
                attachments.add(new DocumentUpload(name, name, fileMimeTypes.get(name), content)));
        return attachments;
    }
}

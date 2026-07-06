package com.rhsystem.interfaces.ui.pages.usuario;

import com.rhsystem.application.dto.usuario.DocumentUpload;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.interfaces.ui.component.DocumentField;
import com.rhsystem.interfaces.ui.component.Shuttle;
import com.rhsystem.interfaces.ui.form.Form;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.server.streams.UploadHandler;

import java.nio.Buffer;
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
    private Upload upload;

    public UserForm(boolean editMode, List<Group> availableGroups) {
        super(UserFormModel.class);

        cpf = new DocumentField(getTranslation("field.cpf"), DocumentField.Type.CPF);
        rg  = new DocumentField(getTranslation("field.rg"),  DocumentField.Type.RG);

        TextField firstName = requiredTextField(getTranslation("field.first.name"), "firstName", getTranslation("field.first.name.placeholder"));
        TextField lastName  = requiredTextField(getTranslation("field.last.name"),  "lastName",  getTranslation("field.last.name.placeholder"));
        EmailField email = requiredEmailField(getTranslation("field.email"), "email", getTranslation("field.email.placeholder"));
        configureDocumentFields();
        configureUpload();
        FormLayout personalData = formLayout(4);

        personalData.add(firstName,2);
        personalData.add(lastName,2);
        personalData.add(cpf,2);
        personalData.add(rg,2);
        personalData.add(email,4);




        Shuttle<Group> groups = shuttle(getTranslation("field.groups"), "groups", availableGroups, Group::getName);
        groups.setCaptions(getTranslation("field.groups.available"), getTranslation("field.groups.chosen"));
        FormLayout groupsContent = formLayout();
        groupsContent.add(groups, 2);

        FormLayout address = formLayout(
                textField(getTranslation("field.street"),        "street"),
                textField(getTranslation("field.street.number"), "streetNumber"),
                textField(getTranslation("field.neighborhood"),  "neighborhood"),
                textField(getTranslation("field.complement"),    "complement"),
                textField(getTranslation("field.postal.code"),   "postalCode"));

        TabSheet tabs = tabSheet();
        tabs.add(getTranslation("form.user.tab.groups"),   groupsContent);
        tabs.add(getTranslation("form.user.tab.address"),  address);

        if (!editMode) {

            tabs.add(getTranslation("form.user.tab.documents"), upload);
        }
        add(personalData, tabs);
    }

    private void configureUpload() {
        var uploadHandler = UploadHandler.inMemory((metadata, data) -> {
            fileMimeTypes.put(metadata.fileName(), metadata.contentType());
            fileContents.put(metadata.fileName(), data);
        });
        upload = new Upload(uploadHandler);
        UploadI18N i18n = new UploadI18N();

        var drop = new UploadI18N.DropFiles();
        drop.setOne(getTranslation("form.user.documents.drop"));
        drop.setMany(getTranslation("form.user.documents.drop"));

        var files = new UploadI18N.AddFiles();
        files.setOne(getTranslation("upload.file.label"));
        files.setMany(getTranslation("upload.files.label"));
        i18n.setAddFiles(files);
        i18n.setDropFiles(drop);
        upload.setWidthFull();
        upload.setI18n(i18n);
    
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

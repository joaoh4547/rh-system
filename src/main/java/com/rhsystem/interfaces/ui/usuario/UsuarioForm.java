package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.application.dto.DocumentoUpload;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.interfaces.ui.component.CampoDocumento;
import com.rhsystem.interfaces.ui.form.Form;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Formulário de cadastro/edição de Usuário construído sobre {@link Form},
 * usando as fábricas de campos e os helpers de bind.
 */
public class UsuarioForm extends Form<UsuarioFormModel> {

    private final CampoDocumento cpf = new CampoDocumento("CPF", CampoDocumento.Tipo.CPF);
    private final CampoDocumento rg = new CampoDocumento("RG", CampoDocumento.Tipo.RG);
    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final Upload upload = new Upload(buffer);

    public UsuarioForm(boolean edicao) {
        super(UsuarioFormModel.class);

        TextField nome = requiredTextField("Nome", "nome", "Informe o nome");
        TextField sobrenome = requiredTextField("Sobrenome", "sobrenome", "Informe o sobrenome");
        configurarDocumentos();
        FormLayout dadosPessoais = formLayout(nome, sobrenome, cpf, rg);

        EmailField email = requiredEmailField("Email", "email", "Informe o email");
        FormLayout acesso;
        if (edicao) {
            var status = comboBox("Status", "status",
                    List.of(StatusUsuario.values()), StatusUsuario::getDescricao);
            acesso = formLayout(email, status);
        } else {
            acesso = formLayout(email);
        }

        FormLayout endereco = formLayout(
                textField("Logradouro", "logradouro"),
                textField("Número", "numero"),
                textField("Bairro", "bairro"),
                textField("Complemento", "complemento"),
                textField("CEP", "cep"));

        TabSheet tabs = tabSheet();
        tabs.add("Dados pessoais", dadosPessoais);
        tabs.add("Acesso", acesso);
        tabs.add("Endereço", endereco);

        if (!edicao) {
            upload.setDropLabel(new Span("Arraste ou selecione os documentos solicitados"));
            upload.setWidthFull();
            tabs.add("Documentos", upload);
        }
        add(tabs);
    }

    /** CPF/RG usam campo com máscara; o binder guarda apenas os dígitos no modelo. */
    private void configurarDocumentos() {
        cpf.setWidthFull();
        cpf.setRequiredIndicatorVisible(true);
        rg.setWidthFull();
        rg.setRequiredIndicatorVisible(true);

        getBinder().forField(cpf).asRequired("Informe o CPF")
                .withConverter(v -> v == null ? "" : v.replaceAll("\\D", ""), d -> d == null ? "" : d)
                .bind(UsuarioFormModel::getCpf, UsuarioFormModel::setCpf);

        getBinder().forField(rg).asRequired("Informe o RG")
                .withConverter(v -> v == null ? "" : v.replaceAll("[^A-Za-z0-9]", ""), d -> d == null ? "" : d)
                .bind(UsuarioFormModel::getRg, UsuarioFormModel::setRg);
    }

    /** Lê os anexos enviados (apenas no cadastro). */
    public List<DocumentoUpload> lerAnexos() {
        List<DocumentoUpload> anexos = new ArrayList<>();
        for (String nomeArquivo : buffer.getFiles()) {
            try (InputStream in = buffer.getInputStream(nomeArquivo)) {
                byte[] conteudo = in.readAllBytes();
                String mime = buffer.getFileData(nomeArquivo).getMimeType();
                anexos.add(new DocumentoUpload(nomeArquivo, nomeArquivo, mime, conteudo));
            } catch (IOException e) {
                throw new RegraNegocioException("Falha ao ler o anexo: " + nomeArquivo);
            }
        }
        return anexos;
    }
}

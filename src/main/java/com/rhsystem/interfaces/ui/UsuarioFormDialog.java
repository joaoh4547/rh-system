package com.rhsystem.interfaces.ui;

import com.rhsystem.application.dto.AtualizarUsuarioCommand;
import com.rhsystem.application.dto.DocumentoUpload;
import com.rhsystem.application.dto.EnderecoDTO;
import com.rhsystem.application.dto.NovoUsuarioCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.service.UsuarioService;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.interfaces.ui.component.CampoDocumento;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
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
 * Diálogo de cadastro/edição de usuário, organizado em seções.
 */
public class UsuarioFormDialog extends Dialog {

    private final UsuarioService usuarioService;
    private final Usuario edicao;
    private final Runnable onSaved;

    private final TextField nome = new TextField("Nome");
    private final TextField sobrenome = new TextField("Sobrenome");
    private final EmailField email = new EmailField("Email");
    private final CampoDocumento cpf = new CampoDocumento("CPF", CampoDocumento.Tipo.CPF);
    private final CampoDocumento rg = new CampoDocumento("RG", CampoDocumento.Tipo.RG);
    private final ComboBox<StatusUsuario> status = new ComboBox<>("Status");

    private final TextField logradouro = new TextField("Logradouro");
    private final TextField bairro = new TextField("Bairro");
    private final TextField numero = new TextField("Número");
    private final TextField complemento = new TextField("Complemento");
    private final TextField cep = new TextField("CEP");

    private final MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
    private final Upload upload = new Upload(buffer);

    public UsuarioFormDialog(UsuarioService usuarioService, Usuario edicao, Runnable onSaved) {
        this.usuarioService = usuarioService;
        this.edicao = edicao;
        this.onSaved = onSaved;

        setHeaderTitle(edicao == null ? "Novo usuário" : "Editar usuário");
        setModal(true);
        setDraggable(true);
        setWidth("680px");

        add(criarConteudo());
        configurarRodape();

        if (edicao != null) {
            preencher(edicao);
        }
    }

    private TabSheet criarConteudo() {
        // Dados pessoais
        nome.setRequiredIndicatorVisible(true);
        sobrenome.setRequiredIndicatorVisible(true);
        FormLayout dadosPessoais = secao(nome, sobrenome, cpf, rg);

        // Acesso
        email.setRequiredIndicatorVisible(true);
        email.setWidthFull();
        FormLayout acesso;
        if (edicao != null) {
            status.setItems(StatusUsuario.values());
            status.setItemLabelGenerator(StatusUsuario::getDescricao);
            acesso = secao(email, status);
        } else {
            acesso = secao(email);
        }

        // Endereço
        FormLayout endereco = secao(logradouro, numero, bairro, complemento, cep);

        TabSheet tabs = new TabSheet();
        tabs.setWidthFull();
        tabs.add("Dados pessoais", dadosPessoais);
        tabs.add("Acesso", acesso);
        tabs.add("Endereço", endereco);

        if (edicao == null) {
            upload.setDropLabel(new Span("Arraste ou selecione os documentos solicitados"));
            upload.setWidthFull();
            tabs.add("Documentos", upload);
        }
        return tabs;
    }

    private FormLayout secao(com.vaadin.flow.component.Component... campos) {
        FormLayout form = new FormLayout(campos);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("480px", 2));
        form.getStyle().set("padding-top", "var(--lumo-space-s)");
        return form;
    }

    private void configurarRodape() {
        Button salvar = new Button("Salvar", e -> salvar());
        salvar.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelar = new Button("Cancelar", e -> close());
        cancelar.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        getFooter().add(cancelar, salvar);
    }

    private void preencher(Usuario u) {
        nome.setValue(nvl(u.getNome()));
        sobrenome.setValue(nvl(u.getSobrenome()));
        email.setValue(nvl(u.getEmail()));
        cpf.setDigitos(nvl(u.getCpf()));
        rg.setDigitos(nvl(u.getRg()));
        status.setValue(u.getStatus());
        if (u.getEndereco() != null) {
            logradouro.setValue(nvl(u.getEndereco().getLogradouro()));
            bairro.setValue(nvl(u.getEndereco().getBairro()));
            numero.setValue(nvl(u.getEndereco().getNumero()));
            complemento.setValue(nvl(u.getEndereco().getComplemento()));
            cep.setValue(nvl(u.getEndereco().getCep()));
        }
    }

    private void salvar() {
        try {
            EnderecoDTO endereco = new EnderecoDTO(
                    logradouro.getValue(), bairro.getValue(), numero.getValue(),
                    complemento.getValue(), cep.getValue());

            if (edicao == null) {
                usuarioService.criar(new NovoUsuarioCommand(
                        nome.getValue(), sobrenome.getValue(), email.getValue(),
                        cpf.getDigitos(), rg.getDigitos(), endereco, lerAnexos()));
                Notification.show("Usuário criado. Email de ativação enviado.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            } else {
                usuarioService.atualizar(new AtualizarUsuarioCommand(
                        edicao.getId(), nome.getValue(), sobrenome.getValue(), email.getValue(),
                        cpf.getDigitos(), rg.getDigitos(), status.getValue(), endereco));
                Notification.show("Usuário atualizado.")
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            }
            onSaved.run();
            close();
        } catch (RegraNegocioException ex) {
            Notification.show(ex.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    private List<DocumentoUpload> lerAnexos() {
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

    private static String nvl(String s) {
        return s == null ? "" : s;
    }
}

package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.application.dto.AtualizarUsuarioCommand;
import com.rhsystem.application.dto.EnderecoDTO;
import com.rhsystem.application.dto.NovoUsuarioCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.application.usecase.usuario.AtualizarUsuario;
import com.rhsystem.application.usecase.usuario.CriarUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.interfaces.ui.form.FormDialog;
import com.rhsystem.interfaces.ui.form.FormDialogAction;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Diálogo de cadastro/edição de Usuário, construído sobre {@link FormDialog}
 * e {@link UsuarioForm}.
 */
public class UsuarioFormDialog extends FormDialog<UsuarioFormModel> {

    private final CriarUsuario criarUsuario;
    private final AtualizarUsuario atualizarUsuario;
    private final Usuario edicao;
    private final Runnable onSaved;
    private final UsuarioForm form;

    public UsuarioFormDialog(CriarUsuario criarUsuario, AtualizarUsuario atualizarUsuario,
                             Usuario edicao, Runnable onSaved) {
        super(edicao == null ? "Novo usuário" : "Editar usuário", new UsuarioForm(edicao != null));
        this.criarUsuario = criarUsuario;
        this.atualizarUsuario = atualizarUsuario;
        this.edicao = edicao;
        this.onSaved = onSaved;
        this.form = (UsuarioForm) getForm();

        width("680px");
        actions(
                FormDialogAction.cancel(),
                FormDialogAction.primary("Salvar", this::salvar));

        getForm().setBean(edicao == null ? new UsuarioFormModel() : UsuarioFormModel.from(edicao));
    }

    private void salvar() {
        UsuarioFormModel model = getForm().getBean();
        if (!getForm().writeBeanIfValid(model)) {
            return; // o binder já destaca os campos inválidos
        }
        try {
            EnderecoDTO endereco = new EnderecoDTO(model.getLogradouro(), model.getBairro(),
                    model.getNumero(), model.getComplemento(), model.getCep());

            if (edicao == null) {
                criarUsuario.executar(new NovoUsuarioCommand(
                        model.getNome(), model.getSobrenome(), model.getEmail(),
                        model.getCpf(), model.getRg(), endereco, form.lerAnexos()));
                notificar("Usuário criado. Email de ativação enviado.", true);
            } else {
                atualizarUsuario.executar(new AtualizarUsuarioCommand(
                        edicao.getId(), model.getNome(), model.getSobrenome(), model.getEmail(),
                        model.getCpf(), model.getRg(), model.getStatus(), endereco));
                notificar("Usuário atualizado.", true);
            }
            onSaved.run();
            close();
        } catch (RegraNegocioException ex) {
            notificar(ex.getMessage(), false);
        }
    }

    private void notificar(String msg, boolean sucesso) {
        Notification.show(msg).addThemeVariants(
                sucesso ? NotificationVariant.LUMO_SUCCESS : NotificationVariant.LUMO_ERROR);
    }
}

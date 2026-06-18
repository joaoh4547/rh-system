package com.rhsystem.interfaces.ui.usuario;

import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bean editável pelo formulário de Usuário (ligado via Binder). */
@Getter
@Setter
@NoArgsConstructor
public class UsuarioFormModel {

    private String nome;
    private String sobrenome;
    private String email;
    private String cpf;
    private String rg;
    private StatusUsuario status;

    private String logradouro;
    private String bairro;
    private String numero;
    private String complemento;
    private String cep;

    /** Cria o modelo a partir de um usuário existente (edição). */
    public static UsuarioFormModel from(Usuario u) {
        UsuarioFormModel m = new UsuarioFormModel();
        m.nome = u.getNome();
        m.sobrenome = u.getSobrenome();
        m.email = u.getEmail();
        m.cpf = u.getCpf();
        m.rg = u.getRg();
        m.status = u.getStatus();
        if (u.getEndereco() != null) {
            m.logradouro = u.getEndereco().getLogradouro();
            m.bairro = u.getEndereco().getBairro();
            m.numero = u.getEndereco().getNumero();
            m.complemento = u.getEndereco().getComplemento();
            m.cep = u.getEndereco().getCep();
        }
        return m;
    }
}

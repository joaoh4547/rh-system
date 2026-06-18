package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.AtivacaoCommand;
import com.rhsystem.application.dto.EnderecoDTO;
import com.rhsystem.application.dto.NovoUsuarioCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.domain.model.usuario.Endereco;

/** Funções utilitárias compartilhadas entre os use cases de Usuario. */
final class UsuarioSupport {

    private UsuarioSupport() {
    }

    static Endereco toEndereco(EnderecoDTO dto) {
        if (dto == null) {
            return new Endereco();
        }
        return new Endereco(dto.logradouro(), dto.bairro(), dto.numero(), dto.complemento(), dto.cep());
    }

    static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    static String somenteAlfanumerico(String valor) {
        return valor == null ? "" : valor.replaceAll("[^A-Za-z0-9]", "");
    }

    static void validarObrigatorios(NovoUsuarioCommand cmd, String cpf, String rg) {
        if (isBlank(cmd.nome()) || isBlank(cmd.sobrenome())) {
            throw new RegraNegocioException("Nome e sobrenome são obrigatórios.");
        }
        if (isBlank(cmd.email())) {
            throw new RegraNegocioException("Email é obrigatório.");
        }
        if (isBlank(cpf) || isBlank(rg)) {
            throw new RegraNegocioException("CPF e RG são obrigatórios.");
        }
    }

    static void validarSenha(AtivacaoCommand cmd) {
        if (cmd.senha() == null || cmd.senha().length() < 6) {
            throw new RegraNegocioException("A senha deve ter ao menos 6 caracteres.");
        }
        if (!cmd.senha().equals(cmd.confirmacaoSenha())) {
            throw new RegraNegocioException("A senha e a confirmação não conferem.");
        }
    }
}

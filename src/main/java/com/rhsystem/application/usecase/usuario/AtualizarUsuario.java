package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.AtualizarUsuarioCommand;
import com.rhsystem.application.exception.RegraNegocioException;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import com.rhsystem.domain.service.ValidadorCpf;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Use case: atualiza os dados de um usuário existente. */
@Service
public class AtualizarUsuario {

    private final UsuarioRepository usuarioRepository;

    public AtualizarUsuario(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public Usuario executar(AtualizarUsuarioCommand cmd) {
        Usuario usuario = usuarioRepository.buscarPorId(cmd.id())
                .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado."));

        String cpf = ValidadorCpf.apenasDigitos(cmd.cpf());
        String rg = UsuarioSupport.somenteAlfanumerico(cmd.rg());
        if (!ValidadorCpf.isValido(cpf)) {
            throw new RegraNegocioException("CPF inválido.");
        }
        if (!usuario.getEmail().equalsIgnoreCase(cmd.email()) && usuarioRepository.existePorEmail(cmd.email())) {
            throw new RegraNegocioException("Já existe um usuário com este email.");
        }
        if (!usuario.getCpf().equals(cpf) && usuarioRepository.existePorCpf(cpf)) {
            throw new RegraNegocioException("Já existe um usuário com este CPF.");
        }
        if (!usuario.getRg().equals(rg) && usuarioRepository.existePorRg(rg)) {
            throw new RegraNegocioException("Já existe um usuário com este RG.");
        }

        usuario.setNome(cmd.nome().trim());
        usuario.setSobrenome(cmd.sobrenome().trim());
        usuario.setEmail(cmd.email().trim());
        usuario.setCpf(cpf);
        usuario.setRg(rg);
        usuario.setStatus(cmd.status());
        usuario.setEndereco(UsuarioSupport.toEndereco(cmd.endereco()));
        usuario.setAtualizadoEm(LocalDateTime.now());
        return usuarioRepository.salvar(usuario);
    }
}

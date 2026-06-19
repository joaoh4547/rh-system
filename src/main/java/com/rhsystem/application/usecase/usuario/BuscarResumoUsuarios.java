package com.rhsystem.application.usecase.usuario;

import com.rhsystem.application.dto.ResumoUsuarios;
import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case: busca o resumo estatístico de usuários calculado no banco.
 *
 * <p>Não carrega registros em memória — executa apenas contagens por status,
 * sendo adequado para exibição de KPIs em dashboards paginados.
 */
@Service
public class BuscarResumoUsuarios {

    private final UsuarioRepository usuarioRepository;

    public BuscarResumoUsuarios(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public ResumoUsuarios executar() {
        return new ResumoUsuarios(
                usuarioRepository.contar(),
                usuarioRepository.contarPorStatus(StatusUsuario.ATIVO),
                usuarioRepository.contarPorStatus(StatusUsuario.PENDENTE_CONFIRMACAO),
                usuarioRepository.contarPorStatus(StatusUsuario.BLOQUEADO)
        );
    }
}

package com.rhsystem.infrastructure.config;

import com.rhsystem.domain.model.usuario.StatusUsuario;
import com.rhsystem.domain.model.usuario.Usuario;
import com.rhsystem.infrastructure.persistence.JpaUsuarioRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Carrega o usuário para autenticação a partir do username.
 * Apenas usuários ATIVOS conseguem autenticar.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final JpaUsuarioRepository repository;

    public UsuarioDetailsService(JpaUsuarioRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        boolean ativo = usuario.getStatus() == StatusUsuario.ATIVO;
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getSenha() == null ? "" : usuario.getSenha())
                .disabled(!ativo)
                .accountLocked(usuario.getStatus() == StatusUsuario.BLOQUEADO)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }
}

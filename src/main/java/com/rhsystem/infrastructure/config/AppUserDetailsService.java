package com.rhsystem.infrastructure.config;

import com.rhsystem.domain.model.usuario.User;
import com.rhsystem.domain.model.usuario.UserStatus;
import com.rhsystem.infrastructure.persistence.JpaUserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

/**
 * Loads the user for authentication from the username.
 * Only ACTIVE users can authenticate.
 */
@Service
public class AppUserDetailsService implements UserDetailsService {

    private final JpaUserRepository repository;

    public AppUserDetailsService(JpaUserRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = repository.findByUsername(username)
                // Generic message, no username: never reveals whether an account exists.
                // Spring's DaoAuthenticationProvider hides this as BadCredentialsException and
                // runs a dummy password check, so response time does not leak it either.
                .orElseThrow(() -> new UsernameNotFoundException("Bad credentials"));

        boolean active = user.getStatus() == UserStatus.ACTIVE;
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword() == null ? "" : user.getPassword())
                .disabled(!active)
                .accountLocked(user.getStatus() == UserStatus.BLOCKED)
                .authorities(makeAuthorities(user))
                .build();
    }

    private Collection<SimpleGrantedAuthority> makeAuthorities(User user) {
        return user.getUserFunctionalities().stream().map(f -> new SimpleGrantedAuthority(f.asRole())).toList();
    }
}

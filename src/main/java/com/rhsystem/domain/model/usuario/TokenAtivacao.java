package com.rhsystem.domain.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Token de ativação enviado por email. Possui prazo de validade e uso único.
 */
@Entity
@Table(name = "token_ativacao")
@Getter
@Setter
@NoArgsConstructor
public class TokenAtivacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, updatable = false)
    private String token;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "expira_em", nullable = false)
    private LocalDateTime expiraEm;

    @Column(name = "usado", nullable = false)
    private boolean usado = false;

    public TokenAtivacao(Usuario usuario, LocalDateTime expiraEm) {
        this.usuario = usuario;
        this.token = UUID.randomUUID().toString();
        this.expiraEm = expiraEm;
        this.usado = false;
    }

    public boolean isValido() {
        return !usado && LocalDateTime.now().isBefore(expiraEm);
    }
}

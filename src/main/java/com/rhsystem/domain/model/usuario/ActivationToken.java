package com.rhsystem.domain.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * Activation token sent by email. Single-use with an expiry time.
 */
@Entity
@Table(name = "rh_activation_token")
@Getter
@Setter
@NoArgsConstructor
public class ActivationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, updatable = false)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "used", nullable = false)
    private boolean used = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", nullable = false)
    private TokenPurpose purpose = TokenPurpose.ACTIVATION;

    public ActivationToken(User user, LocalDateTime expiresAt, TokenPurpose purpose) {
        this.user = user;
        this.token = UUID.randomUUID().toString();
        this.expiresAt = expiresAt;
        this.used = false;
        this.purpose = purpose;
    }

    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
}

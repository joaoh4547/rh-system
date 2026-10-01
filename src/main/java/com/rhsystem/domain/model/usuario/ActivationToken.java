package com.rhsystem.domain.model.usuario;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Activation / password-reset token sent by email. Single-use with an expiry time.
 *
 * <p><b>Only the SHA-256 hash of the token is persisted</b> (column {@code token}).
 * The raw value exists only in memory right after creation ({@link #getRawToken()}),
 * long enough to be emailed. Whoever reads the database (backup, SQL injection,
 * insider) cannot use the stored values to take over accounts. A random UUID has
 * 122 bits of entropy, so an unsalted fast hash is adequate here (unlike passwords).</p>
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

    /** SHA-256 (hex) of the raw token. */
    @Setter(AccessLevel.NONE)
    @Column(name = "token", nullable = false, unique = true, updatable = false)
    private String tokenHash;

    /** Raw token — never persisted; only set on freshly created instances. */
    @Setter(AccessLevel.NONE)
    @Transient
    private String rawToken;

    // A user may have several tokens over time (activation, then resets): many-to-one.
    @ManyToOne
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
        this.rawToken = UUID.randomUUID().toString();
        this.tokenHash = hash(rawToken);
        this.expiresAt = expiresAt;
        this.used = false;
        this.purpose = purpose;
    }

    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }

    /** SHA-256 hex of a raw token, as stored in the database. */
    public static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}

package com.rhsystem.domain.model.usuario;

import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.grupo.Group;
import com.rhsystem.infrastructure.config.CacheConfig;
import com.rhsystem.utils.CacheEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate root (DDD) representing a system user.
 */
@Entity
@Table(
        name = "rh_user",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rh_user_username", columnNames = "username"),
                @UniqueConstraint(name = "uk_rh_user_email", columnNames = "email"),
                @UniqueConstraint(name = "uk_rh_user_cpf", columnNames = "cpf"),
                @UniqueConstraint(name = "uk_rh_user_rg", columnNames = "rg")
        }
)
@Getter
@Setter
@NoArgsConstructor
@CacheEntity(cacheName = CacheConfig.USERS)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "username", nullable = false, updatable = false)
    private String username;

    @Column(name = "email", nullable = false)
    private String email;

    /**
     * BCrypt password hash. Null until the user activates the account.
     */
    @Column(name = "password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private UserStatus status = UserStatus.PENDING_CONFIRMATION;

    /**
     * Digits only.
     */
    @Column(name = "cpf", nullable = false, length = 11)
    private String cpf;

    @Column(name = "rg", nullable = false)
    private String rg;

    @Embedded
    private Address address = new Address();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Document> documents = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "rh_user_group",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "group_id"))
    private Collection<Group> groups = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "rh_user_functionality", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "functionality")
    private Collection<Functionality> functionalities = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    /**
     * User's full name.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Adds a document while maintaining aggregate consistency.
     */
    public void addDocument(Document document) {
        document.setUser(this);
        this.documents.add(document);
    }

    /**
     * Completes account activation by setting the password hash and marking the account active.
     */
    public void activate(String passwordHash) {
        this.password = passwordHash;
        this.status = UserStatus.ACTIVE;
    }

    /**
     * Sets a new password hash.
     */
    public void resetPassword(String passwordHash) {
        this.password = passwordHash;
    }

    /**
     * Records acceptance of the terms of use.
     */
    public void acceptTerms() {
        this.termsAcceptedAt = LocalDateTime.now();
    }

    public boolean termsAccepted() {
        return termsAcceptedAt != null;
    }

    public boolean isAdmin() {
        return groups.stream().anyMatch(Group::isAdmin);
    }

    public Set<Functionality> getUserFunctionalities() {
        var func = new HashSet<Functionality>();
        if (isAdmin()) {
            func.addAll(List.of(Functionality.values()));
        } else {
            func.addAll(this.functionalities);
            groups.stream().filter(Group::isEnable).forEach(g -> func.addAll(g.getFunctionalities()));
        }
        return func;
    }
}

package com.rhsystem.domain.model.grupo;

import com.rhsystem.domain.model.Functionality;
import com.rhsystem.domain.model.shared.HasEnable;
import com.rhsystem.infrastructure.config.CacheConfig;
import com.rhsystem.utils.CacheEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;

@Entity
@Table(name = "rh_group")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@CacheEntity(cacheName = CacheConfig.GROUPS)
public class Group implements Serializable, HasEnable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    /** Default {@code true} both for {@code new Group()} and for the builder. */
    @Builder.Default
    @Column(name = "active")
    private boolean enable = true;

    @Column(name = "admin")
    private boolean admin;

    @ElementCollection
    @CollectionTable(name = "rh_group_functionality", joinColumns = @JoinColumn(name = "group_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "functionality")
    private Collection<Functionality> functionalities;

    /**
     * Entity identity: two Groups are the same when they share a persistent id,
     * regardless of the JPA session/cache instance they came from.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}

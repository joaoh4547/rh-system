package com.rhsystem.domain.model.grupo;

import com.rhsystem.domain.model.Functionality;
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
public class Group implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "admin")
    private boolean admin = false;

    @ElementCollection
    @CollectionTable(name = "rh_group_functionality", joinColumns = @JoinColumn(name = "group_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "functionality")
    private Collection<Functionality> functionalities;
}
